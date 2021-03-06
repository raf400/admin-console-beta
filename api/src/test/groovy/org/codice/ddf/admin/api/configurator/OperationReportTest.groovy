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
package org.codice.ddf.admin.api.configurator

import spock.lang.Specification

class OperationReportTest extends Specification {
    OperationReport report

    def setup() {
        report = new OperationReport()
    }

    def 'check report with no failures'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def pass2 = OperationReport.Result.pass()

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', pass2)

        then:
        report.txactSucceeded()
        !report.containsFailedResults()
        report.getFailedResults() == []
        report.getResult('a1') == pass1
        report.getResult('b2') == pass2
    }

    def 'report with failures'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def throwable = Mock(Throwable)
        def fail1 = OperationReport.Result.fail(throwable)

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', fail1)

        then:
        !report.txactSucceeded()
        report.containsFailedResults()
        report.getFailedResults() == [fail1]
        report.getResult('a1') == pass1
        report.getResult('b2') == fail1
        report.getResult('b2').badOutcome.get() == throwable
    }

    def 'pass with a managed service'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def pass2 = OperationReport.Result.passManagedService('serviceId1')

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', pass2)

        then:
        report.txactSucceeded()
        !report.containsFailedResults()
        report.getFailedResults() == []
        report.getResult('a1') == pass1
        report.getResult('b2') == pass2
        report.getResult('b2').configId == 'serviceId1'
    }

    def 'rollback success'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def roll1 = OperationReport.Result.rollback()

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', roll1)

        then:
        !report.txactSucceeded()
        report.containsFailedResults()
        report.getFailedResults() == [roll1]
        report.getResult('a1') == pass1
        report.getResult('b2') == roll1
    }

    def 'rollback failed'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def throwable = Mock(Throwable)
        def roll1 = OperationReport.Result.rollbackFail(throwable)

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', roll1)

        then:
        !report.txactSucceeded()
        report.containsFailedResults()
        report.getFailedResults() == [roll1]
        report.getResult('a1') == pass1
        report.getResult('b2') == roll1
        report.getResult('b2').badOutcome.get() == throwable
    }

    def 'rollback failed on a managed service'() {
        setup:
        def pass1 = OperationReport.Result.pass()
        def throwable = Mock(Throwable)
        def roll1 = OperationReport.Result.rollbackFailManagedService(throwable, 'serviceId1')

        when:
        report.putResult('a1', pass1)
        report.putResult('b2', roll1)

        then:
        !report.txactSucceeded()
        report.containsFailedResults()
        report.getFailedResults() == [roll1]
        report.getResult('a1') == pass1
        report.getResult('b2') == roll1
        report.getResult('b2').badOutcome.get() == throwable
        report.getResult('b2').configId == 'serviceId1'
    }

    def 'test skipped rollback step'() {
        setup:
        def roll1 = OperationReport.Result.rollback()
        def skip1 = OperationReport.Result.skip()

        when:
        report.putResult('b2', roll1)
        report.putResult('c3', skip1)

        then:
        !report.txactSucceeded()
        report.containsFailedResults()
        report.getFailedResults() == [roll1, skip1]
        report.getResult('b2') == roll1
        report.getResult('c3') == skip1
    }
}
