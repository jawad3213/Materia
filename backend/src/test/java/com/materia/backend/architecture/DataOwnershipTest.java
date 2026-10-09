package com.materia.backend.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Each context reads and writes only its own tables, so it can move to its own database as a separate service.
 * ArchUnit checks imports; this checks the SQL and JPQL text, where a table or entity name of another context would
 * otherwise go unnoticed. Shared code ({@code common}) names no context's table at all.
 *
 * <p>Only strings that look like queries are checked, so messages that merely mention "suppliers" are fine. Dev data
 * seeders are left out: they fill a local database and are not part of any service's behaviour.
 */
class DataOwnershipTest {

    private static final Path MAIN = Paths.get("src/main/java/com/materia/backend");
    private static final Pattern CONTEXT = Pattern.compile("contexts[/\\\\]([^/\\\\]+)[/\\\\]");
    private static final Pattern TABLE = Pattern.compile("@Table\\(\\s*name\\s*=\\s*\"([a-z_]+)\"");
    /** A Java string literal; possessive, so a long literal cannot overflow the regex engine's stack. */
    private static final Pattern STRING = Pattern.compile("\"((?:[^\"\\\\\\n]++|\\\\.)*+)\"");
    private static final Pattern QUERY = Pattern.compile("(?i)\\b(select|from|join|update|insert into|delete from)\\b");

    private record Owned(String name, String context) {
    }

    @Test
    @DisplayName("data: no SQL or JPQL names another context's table or entity")
    void contextsQueryOnlyTheirOwnData() throws IOException {
        List<Owned> owned = ownedNames();
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String path = file.toString().replace('\\', '/');
                if (path.contains("/infrastructure/seed/")) continue;
                String here = contextOf(path);
                String source = Files.readString(file, StandardCharsets.UTF_8);
                Matcher string = STRING.matcher(source);
                while (string.find()) {
                    String text = string.group(1);
                    if (!QUERY.matcher(text).find()) continue;
                    for (Owned name : owned) {
                        if (!name.context().equals(here)
                                && Pattern.compile("\\b" + Pattern.quote(name.name()) + "\\b").matcher(text).find()) {
                            violations.add(MAIN.relativize(file) + " uses " + name.name() + " of " + name.context()
                                    + ": \"" + text.strip() + "\"");
                        }
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "Queries on another context's data:\n" + String.join("\n", violations));
    }

    /** Table and JPA entity names, with the context that owns them. */
    private static List<Owned> ownedNames() throws IOException {
        Map<String, String> owners = new HashMap<>();
        try (Stream<Path> files = Files.walk(MAIN.resolve("contexts"))) {
            for (Path file : files.filter(p -> p.toString().endsWith("JpaEntity.java")).toList()) {
                String context = contextOf(file.toString().replace('\\', '/'));
                String fileName = file.getFileName().toString();
                owners.put(fileName.substring(0, fileName.length() - ".java".length()), context);
                Matcher table = TABLE.matcher(Files.readString(file, StandardCharsets.UTF_8));
                if (table.find()) owners.put(table.group(1), context);
            }
        }
        return owners.entrySet().stream().map(e -> new Owned(e.getKey(), e.getValue())).toList();
    }

    /** The context a source file belongs to, or "common" for shared code. */
    private static String contextOf(String path) {
        Matcher m = CONTEXT.matcher(path);
        return m.find() ? m.group(1) : "common";
    }
}
