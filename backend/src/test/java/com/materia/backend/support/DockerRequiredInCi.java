package com.materia.backend.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * Decides whether a Docker-backed test may run.
 *
 * <ul>
 *   <li><strong>Locally, without Docker:</strong> skipped, and reported as skipped, so a
 *       developer without Docker running still gets a usable fast suite.</li>
 *   <li><strong>In CI:</strong> never skipped. If Docker is unavailable there, the test
 *       runs and fails loudly when the container cannot start.</li>
 * </ul>
 *
 * <p>The asymmetry matters. {@code @Testcontainers(disabledWithoutDocker = true)} would skip
 * in CI as well, so a pipeline that lost Docker would report green without having run a
 * single integration test. A gate that can silently disappear is worse than none, because
 * it is trusted. GitHub Actions sets {@code CI=true} on every run.
 */
public class DockerRequiredInCi implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if ("true".equalsIgnoreCase(System.getenv("CI"))) {
            return ConditionEvaluationResult.enabled("CI: integration tests always run");
        }
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ConditionEvaluationResult.enabled("Docker is available");
            }
        } catch (Throwable t) {
            return ConditionEvaluationResult.disabled(
                    "Docker probe failed or Docker is not running (" + t.getMessage() + "), so integration tests are skipped locally. "
                            + "Start Docker to run them. They are never skipped in CI.");
        }
        return ConditionEvaluationResult.disabled(
                "Docker is not running, so integration tests are skipped locally. "
                        + "Start Docker to run them. They are never skipped in CI.");
    }
}
