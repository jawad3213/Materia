package com.materia.backend.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [T122] Convention guard test (SC-010).
 *
 * <p>Asserts that every test method across in-scope test classes declares
 * a {@link DisplayName} annotation describing the business requirement or rule verified,
 * ensuring test failure outputs are immediately readable and actionable.
 */
class DisplayNameConventionTest {

    @Test
    @DisplayName("convention: every test method must declare a @DisplayName annotation (SC-010)")
    void allTestMethods_haveDisplayName() throws Exception {
        URL resource = getClass().getClassLoader().getResource("com/materia/backend");
        assertThat(resource).isNotNull();

        Path startPath = Paths.get(resource.toURI());
        List<String> violations = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(startPath)) {
            List<Path> classFiles = stream
                    .filter(p -> p.toString().endsWith(".class"))
                    .filter(p -> !p.toString().contains("$")) // skip synthetic inner classes
                    .toList();

            for (Path classFile : classFiles) {
                String relative = startPath.relativize(classFile).toString();
                String className = "com.materia.backend." + relative
                        .replace(File.separatorChar, '.')
                        .replace(".class", "");

                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (Throwable e) {
                    continue; // Skip classes that fail to load
                }

                for (Method method : clazz.getDeclaredMethods()) {
                    boolean isTest = method.isAnnotationPresent(Test.class)
                            || method.isAnnotationPresent(ParameterizedTest.class)
                            || method.isAnnotationPresent(RepeatedTest.class);

                    if (isTest && !method.isAnnotationPresent(DisplayName.class)) {
                        violations.add(className + "#" + method.getName());
                    }
                }
            }
        }

        assertThat(violations)
                .as("All test methods must have @DisplayName (SC-010). Found missing on: %s", violations)
                .isEmpty();
    }
}
