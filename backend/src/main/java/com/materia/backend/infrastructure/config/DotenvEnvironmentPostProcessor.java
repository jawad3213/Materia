package com.materia.backend.infrastructure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the properties of the .env file into the Spring Environment and the System properties.
 *
 * <p>Called by {@code Backend2Application.main()} only. It is deliberately not registered as a post-processor, so
 * that tests never read a developer's .env (see {@code HermeticEnvironmentTest}).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        loadEnv(environment);
    }

    public static void loadEnv(ConfigurableEnvironment environment) {
        File envFile = findEnvFile();
        if (envFile == null || !envFile.exists()) {
            return;
        }

        Map<String, Object> envProperties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envProperties.put(key, value);
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to load .env file: " + e.getMessage());
        }

        if (!envProperties.isEmpty() && environment != null) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envProperties));
        }
    }

    /**
     * The .env file next to the working directory, else the one of the backend module the classes were loaded from
     * ({@code backend/target/classes} or a jar in {@code backend/target}), so an IDE run configuration with another
     * working directory still finds it.
     */
    public static File findEnvFile() {
        List<File> candidates = new ArrayList<>(List.of(
            new File(".env"),
            new File("backend/.env"),
            new File("../.env")
        ));
        File module = moduleDirectory();
        if (module != null) {
            candidates.add(new File(module, ".env"));
            candidates.add(new File(module.getParentFile(), ".env"));
        }
        for (File f : candidates) {
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    /** The module directory above {@code target}, from where this class was loaded; null when unknown. */
    private static File moduleDirectory() {
        try {
            CodeSource source = DotenvEnvironmentPostProcessor.class.getProtectionDomain().getCodeSource();
            if (source == null) {
                return null;
            }
            for (File dir = new File(source.getLocation().toURI()); dir != null; dir = dir.getParentFile()) {
                if ("target".equals(dir.getName())) {
                    return dir.getParentFile();
                }
            }
        } catch (URISyntaxException | SecurityException | IllegalArgumentException e) {
            // not loaded from the file system: only the working directory is searched
        }
        return null;
    }
}
