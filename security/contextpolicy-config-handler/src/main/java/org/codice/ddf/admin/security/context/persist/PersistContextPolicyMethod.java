/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */

package org.codice.ddf.admin.security.context.persist;

import static org.codice.ddf.admin.api.config.security.context.ContextPolicyConfiguration.ALL_FIELDS;
import static org.codice.ddf.admin.api.config.security.context.ContextPolicyConfiguration.CONTEXT_POLICY_BINS;
import static org.codice.ddf.admin.api.config.security.context.ContextPolicyConfiguration.WHITE_LIST_CONTEXTS;
import static org.codice.ddf.admin.api.config.security.context.ContextPolicyConfiguration.buildFieldMap;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.FAILED_PERSIST;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.MessageType.FAILURE;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.MessageType.SUCCESS;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.SUCCESSFUL_PERSIST;
import static org.codice.ddf.admin.api.handler.ConfigurationMessage.buildMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codice.ddf.admin.api.config.security.context.ContextPolicyBin;
import org.codice.ddf.admin.api.config.security.context.ContextPolicyConfiguration;
import org.codice.ddf.admin.api.handler.method.PersistMethod;
import org.codice.ddf.admin.api.handler.report.TestReport;
import org.codice.ddf.admin.api.persist.ConfigReport;
import org.codice.ddf.admin.api.persist.Configurator;

import com.google.common.collect.ImmutableMap;

public class PersistContextPolicyMethod extends PersistMethod<ContextPolicyConfiguration>{

    public static final String PERSIST_CONTEXT_POLICY_ID = "persist";

    public static final String DESCRIPTION =
            "Persist changes to the Web Context Policy manager.";

    public static final Map<String, String> REQUIRED_FIELDS = buildFieldMap(CONTEXT_POLICY_BINS,
            WHITE_LIST_CONTEXTS);

    public static final Map<String, String> SUCCESS_TYPES = ImmutableMap.of(SUCCESSFUL_PERSIST, "Successfully saved Web Context Policy Manager settings");

    public static final Map<String, String> FAILURE_TYPES = ImmutableMap.of(FAILED_PERSIST, "Unable to persist changes");
    public PersistContextPolicyMethod() {
        super(PERSIST_CONTEXT_POLICY_ID,
                DESCRIPTION,
                REQUIRED_FIELDS,
                null,
                SUCCESS_TYPES,
                FAILURE_TYPES,
                null);
    }

    @Override
    public TestReport persist(ContextPolicyConfiguration config) {
        TestReport report = new TestReport(config.validate(ALL_FIELDS));
        if(report.containsFailureMessages()) {
            return report;
        }

        Configurator configurator = new Configurator();
        configurator.updateConfigFile("org.codice.ddf.security.policy.context.impl.PolicyManager",
                configToPolicyManagerSettings(config),
                true);
        ConfigReport configReport = configurator.commit();

        if (!configReport.getFailedResults()
                .isEmpty()) {
            return new TestReport(buildMessage(FAILURE, FAILED_PERSIST, FAILURE_TYPES.get(FAILED_PERSIST)));
        } else {
            return new TestReport(buildMessage(SUCCESS, SUCCESSFUL_PERSIST, SUCCESS_TYPES.get(SUCCESSFUL_PERSIST)));
        }
    }

    public Map<String, Object> configToPolicyManagerSettings(ContextPolicyConfiguration config){
        List<String> realmsProps = new ArrayList<>();
        List<String> authTypesProps = new ArrayList<>();
        List<String> reqAttrisProps = new ArrayList<>();

        for (ContextPolicyBin bin : config.contextPolicyBins()) {
            bin.contextPaths()
                    .stream()
                    .forEach(context -> {
                        realmsProps.add(context + "=" + bin.realm());
                        authTypesProps.add(
                                context + "=" + String.join("|", bin.authenticationTypes()));
                        if (bin.requiredAttributes()
                                .isEmpty()) {
                            reqAttrisProps.add(context + "=");
                        } else {
                            reqAttrisProps.add(context + "={" + String.join(";",
                                    bin.requiredAttributes()
                                            .entrySet()
                                            .stream()
                                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                                            .collect(Collectors.toList())) + "}");
                        }
                    });
        }

        return ImmutableMap.of("authenticationTypes",
                authTypesProps,
                "realms",
                realmsProps,
                "requiredAttributes",
                reqAttrisProps,
                "whiteListContexts",
                config.whiteListContexts());
    }
}