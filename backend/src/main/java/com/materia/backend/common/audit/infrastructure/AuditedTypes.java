package com.materia.backend.common.audit.infrastructure;

import com.materia.backend.common.audit.application.Audited;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The record types whose history can be read, and the permission each needs: collected once from the
 * {@link Audited} entities found on the classpath, so the history API lists no context itself.
 */
public final class AuditedTypes {

    private static final String BASE_PACKAGE = "com.materia.backend";
    private static volatile Map<String, Optional<String>> readPermissions;

    private AuditedTypes() {
    }

    /**
     * Empty when the type is unknown; else the permission that reads it (itself empty when only log:read may).
     */
    public static Optional<Optional<String>> readPermissionOf(String historyType) {
        return Optional.ofNullable(all().get(historyType));
    }

    private static Map<String, Optional<String>> all() {
        Map<String, Optional<String>> result = readPermissions;
        if (result == null) {
            synchronized (AuditedTypes.class) {
                if (readPermissions == null) readPermissions = scan();
                result = readPermissions;
            }
        }
        return result;
    }

    private static Map<String, Optional<String>> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Audited.class));
        Map<String, Optional<String>> found = new HashMap<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> type = ClassUtils.resolveClassName(candidate.getBeanClassName(), AuditedTypes.class.getClassLoader());
            Audited audited = type.getAnnotation(Audited.class);
            if (audited.parent().isEmpty()) {
                found.put(audited.type(), audited.readPermission().isEmpty()
                        ? Optional.empty() : Optional.of(audited.readPermission()));
            }
        }
        return Map.copyOf(found);
    }
}
