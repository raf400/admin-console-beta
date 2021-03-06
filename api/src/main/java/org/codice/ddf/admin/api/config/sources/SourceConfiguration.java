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

package org.codice.ddf.admin.api.config.sources;

import static org.codice.ddf.admin.api.validation.ValidationUtils.validateHostName;
import static org.codice.ddf.admin.api.validation.ValidationUtils.validatePort;
import static org.codice.ddf.admin.api.validation.ValidationUtils.validateString;
import static org.codice.ddf.admin.api.validation.ValidationUtils.validateUrl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.codice.ddf.admin.api.config.Configuration;
import org.codice.ddf.admin.api.config.ConfigurationType;
import org.codice.ddf.admin.api.handler.ConfigurationMessage;
import org.codice.ddf.admin.api.validation.ValidationUtils;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public class SourceConfiguration extends Configuration {

    public static final String CONFIGURATION_TYPE = "sources";

    public static final String SOURCE_NAME = "sourceName";

    public static final String SOURCE_HOSTNAME = "sourceHostName";

    public static final String PORT = "sourcePort";

    public static final String ENDPOINT_URL = "endpointUrl";

    public static final String SOURCE_USERNAME = "sourceUserName";

    public static final String SOURCE_USER_PASSWORD = "sourceUserPassword";

    private String sourceName;

    private String sourceHostName;

    private int sourcePort;

    private String endpointUrl;

    private String sourceUserName;

    private String sourceUserPassword;

    public SourceConfiguration() {
    }

    public SourceConfiguration(SourceConfiguration sourceConfiguration) {
        super(sourceConfiguration);
        this.sourceName = sourceConfiguration.sourceName;
        this.sourceHostName = sourceConfiguration.sourceHostName;
        this.sourcePort = sourceConfiguration.sourcePort;
        this.sourceUserName = sourceConfiguration.sourceUserName;
        this.sourceUserPassword = sourceConfiguration.sourceUserPassword;
        this.endpointUrl = sourceConfiguration.endpointUrl;
    }

    public static <T extends SourceConfiguration> Map<String, Function<T, List<ConfigurationMessage>>> getBaseFieldValidationMap() {
        return new ImmutableMap.Builder<String, Function<T, List<ConfigurationMessage>>>().put(
                SOURCE_NAME,
                config -> validateString(config.sourceName(), SOURCE_NAME))
                .put(SOURCE_HOSTNAME,
                        config -> validateHostName(config.sourceHostName(), SOURCE_HOSTNAME))
                .put(PORT, config -> validatePort(config.sourcePort(), PORT))
                .put(SOURCE_USERNAME,
                        config -> validateString(config.sourceUserName(), SOURCE_USERNAME))
                .put(SOURCE_USER_PASSWORD,
                        config -> validateString(config.sourceUserPassword(), SOURCE_USER_PASSWORD))
                .put(ENDPOINT_URL, config -> validateUrl(config.endpointUrl(), ENDPOINT_URL))
                .put(SERVICE_PID, config -> validateString(config.servicePid(), SERVICE_PID))
                .build();
    }

    public List<ConfigurationMessage> validate(List<String> fields) {
        return ValidationUtils.validate(fields, this, getBaseFieldValidationMap());
    }

    @Override
    public ConfigurationType getConfigurationType() {
        return new ConfigurationType(CONFIGURATION_TYPE, SourceConfiguration.class);
    }

    //Getters
    public int sourcePort() {
        return sourcePort;
    }

    public String sourceName() {
        return sourceName;
    }

    public String endpointUrl() {
        return endpointUrl;
    }

    public String sourceHostName() {
        return sourceHostName;
    }

    public String sourceUserPassword() {
        return sourceUserPassword;
    }

    public String sourceUserName() {
        return sourceUserName;
    }

    //Setters
    public SourceConfiguration sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public SourceConfiguration sourceUserName(String sourceUserName) {
        this.sourceUserName = sourceUserName;
        return this;
    }

    public SourceConfiguration endpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        return this;
    }

    public SourceConfiguration sourceUserPassword(String sourceUserPassword) {
        this.sourceUserPassword = sourceUserPassword;
        return this;
    }

    public SourceConfiguration sourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
        return this;
    }

    public SourceConfiguration sourceHostName(String sourceHostName) {
        this.sourceHostName = sourceHostName;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceName", sourceName)
                .add("sourceHostName", sourceHostName)
                .add("sourcePort", sourcePort)
                .add("endpointUrl", endpointUrl)
                .add("sourceUserName", sourceUserName)
                .add("sourceUserPassword", "********")
                .toString();
    }
}
