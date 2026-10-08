package com.materia.backend.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The bounded contexts stay separable, so each can become its own service (see docs/architecture/modular-monolith.md).
 *
 * <ul>
 *   <li>The domain and application layers of a context know no other context: they talk to the outside through
 *   their own outbound ports.</li>
 *   <li>Code outside a context (its adapters in the other contexts, the gateway) only uses the context's public
 *   API: its inbound ports, application DTOs, and the enums, events and exceptions those expose.</li>
 *   <li>The shared kernel ({@code common}) knows no context.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "com.materia.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

    private static final Pattern CONTEXT_PACKAGE = Pattern.compile("^com\\.materia\\.backend\\.contexts\\.([^.]+)(\\..*)?$");

    /** Packages of a context that other code may use. */
    private static final List<String> PUBLIC_API = List.of(
            ".domain.ports.in", ".application.dtos", ".domain.enums", ".domain.events", ".domain.exceptions");

    @ArchTest
    static final ArchRule domainAndApplicationKnowNoOtherContext =
            classes().that().resideInAnyPackage("com.materia.backend.contexts.*.domain..",
                            "com.materia.backend.contexts.*.application..")
                    .should(new ArchCondition<JavaClass>("depend on no other context") {
                        @Override
                        public void check(JavaClass item, ConditionEvents events) {
                            String own = contextOf(item.getPackageName()).orElseThrow();
                            for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                                contextOf(dependency.getTargetClass().getPackageName())
                                        .filter(target -> !target.equals(own))
                                        .ifPresent(target -> events.add(SimpleConditionEvent.violated(
                                                dependency, dependency.getDescription())));
                            }
                        }
                    });

    @ArchTest
    static final ArchRule otherContextsOnlyUseThePublicApi =
            classes().should(new ArchCondition<JavaClass>("use other contexts only through their public API") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    Optional<String> own = contextOf(item.getPackageName());
                    for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                        String targetPackage = dependency.getTargetClass().getPackageName();
                        Optional<String> target = contextOf(targetPackage);
                        if (target.isEmpty() || target.equals(own)) continue;
                        String inside = targetPackage.substring(("com.materia.backend.contexts." + target.get()).length());
                        boolean publicApi = PUBLIC_API.stream()
                                .anyMatch(api -> inside.equals(api) || inside.startsWith(api + "."));
                        if (!publicApi) {
                            events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                        }
                    }
                }
            });

    @ArchTest
    static final ArchRule sharedKernelKnowsNoContext =
            noClasses().that().resideInAPackage("com.materia.backend.common..")
                    .should().dependOnClassesThat().resideInAPackage("com.materia.backend.contexts..");

    private static Optional<String> contextOf(String packageName) {
        Matcher matcher = CONTEXT_PACKAGE.matcher(packageName);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
