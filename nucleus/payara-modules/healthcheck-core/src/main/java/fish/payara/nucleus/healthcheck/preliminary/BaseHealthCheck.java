/*
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 Copyright (c) 2015 C2B2 Consulting Limited. All rights reserved.
 The contents of this file are subject to the terms of the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License.  You can
 obtain a copy of the License at
 https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 or packager/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 When distributing the software, include this License Header Notice in each
 file and include the License file at packager/legal/LICENSE.txt.
 */
package fish.payara.nucleus.healthcheck.preliminary;

import fish.payara.nucleus.healthcheck.*;
import fish.payara.nucleus.healthcheck.configuration.Checker;
import fish.payara.nucleus.healthcheck.configuration.GarbageCollectorChecker;
import fish.payara.nucleus.healthcheck.configuration.HealthCheckServiceConfiguration;
import fish.payara.nucleus.healthcheck.configuration.ThresholdDiagnosticsChecker;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigExtension;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

/**
 * @author mertcaliskan
 */
@Contract
public abstract class BaseHealthCheck<O extends HealthCheckExecutionOptions, C extends Checker> implements HealthCheckConstants {

    @Inject
    protected HealthCheckService healthCheckService;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    @Optional
    HealthCheckServiceConfiguration configuration;

    protected O options;
    protected Class<C> checkerType;

    public abstract HealthCheckResult doCheck();
    protected abstract O constructOptions(C c);

    protected <T extends BaseHealthCheck> O postConstruct(T t, Class checkerType) {
        this.checkerType = checkerType;
        if (configuration == null) {
            return null;
        }

        C checker = configuration.getCheckerByType(this.checkerType);
        options = constructOptions(checker);
        healthCheckService.registerCheck(checker.getName(), t);

        return options;
    }

    protected HealthCheckExecutionOptions constructBaseOptions(Checker checker) {
        return new HealthCheckExecutionOptions(
                Boolean.valueOf(checker.getEnabled()),
                checker.getTime(),
                asTimeUnit(checker.getUnit()));
    }

    protected TimeUnit asTimeUnit(String unit) {
        return TimeUnit.valueOf(unit);
    }

    protected HealthCheckResultStatus decideOnStatusWithDuration(long duration) {
        if (duration > FIVE_MIN) {
            return HealthCheckResultStatus.CRITICAL;
        }
        else if (duration > ONE_MIN) {
            return HealthCheckResultStatus.WARNING;
        }
        else if (duration > 0) {
            return HealthCheckResultStatus.GOOD;
        }
        else {
            return HealthCheckResultStatus.CHECK_ERROR;
        }
    }

    protected String prettyPrintBytes(long value) {
        String result;

        if (value / ONE_GB > 0) {
            result = (value / ONE_GB) + " Gb";
        }
        else if (value / ONE_MB > 0) {
            result = (value / ONE_MB) + " Mb";
        }
        else if (value / ONE_KB > 0) {
            result = (value / ONE_KB) + " Kb";
        }
        else {
            result = (value) + " bytes";
        }

        return result;
    }

    protected String prettyPrintDuration(long value) {
        long minutes = 0;
        long seconds = 0;
        StringBuilder sb = new StringBuilder();

        if (value > ONE_MIN) {
            minutes = TimeUnit.MILLISECONDS.toMinutes(value);
            value -= TimeUnit.MINUTES.toMillis(minutes);
        }
        if (value > ONE_SEC) {
            seconds = TimeUnit.MILLISECONDS.toSeconds(value);
            value -= TimeUnit.SECONDS.toMillis(seconds);
        }
        if (value >= 0) {
            if (minutes > 0) {
                sb.append(minutes).append(" minutes ");
            }
            if (seconds > 0) {
                sb.append(seconds).append(" seconds ");
            }
            if (value > 0) {
                sb.append(value);
                sb.append(" milliseconds");
            }
            return sb.toString();
        }
        else {
            return null;
        }
    }

    protected String prettyPrintStackTrace(StackTraceElement[] elements) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement traceElement : elements) {
            sb.append("\tat ").append(traceElement);
        }
        return sb.toString();
    }

    public O getOptions() {
        return options;
    }

    public Class<C> getCheckerType() {
        return checkerType;
    }
}
