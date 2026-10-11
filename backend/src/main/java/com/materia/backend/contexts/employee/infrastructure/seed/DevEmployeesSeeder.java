package com.materia.backend.contexts.employee.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOffboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeOnboardingUseCase;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dev only: {@link DevSeed#DOCUMENTS_PER_MODULE} employees in every employment status, the three dev accounts among
 * them, linked to those accounts. No other employee gets a login account: only the dev accounts can sign in. Two are
 * offboarded. Skipped once any employee exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.EMPLOYEES_ORDER)
public class DevEmployeesSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevEmployeesSeeder.class);

    private record Person(String firstName, String lastName, String email) {
    }

    private static final List<Person> PEOPLE = List.of(
            new Person("Amina", "Benali", DevSeed.ADMIN_EMAIL),
            new Person("Youssef", "El Alami", DevSeed.PURCHASER_EMAIL),
            new Person("Leila", "Amrani", DevSeed.RECEIVER_EMAIL),
            new Person("Karim", "Bennani", null), new Person("Sanaa", "Idrissi", null),
            new Person("Tariq", "Mansouri", null), new Person("Nadia", "Chraibi", null),
            new Person("Omar", "Tazi", null), new Person("Salma", "Berrada", null),
            new Person("Hamza", "Alaoui", null), new Person("Imane", "Fassi", null),
            new Person("Mehdi", "Kettani", null), new Person("Zineb", "Lahlou", null),
            new Person("Anas", "Sqalli", null), new Person("Khadija", "Ouazzani", null),
            new Person("Rachid", "Benjelloun", null), new Person("Meryem", "Tahiri", null),
            new Person("Adil", "Bouzidi", null), new Person("Hajar", "Naciri", null),
            new Person("Soufiane", "Lamrani", null), new Person("Ghita", "Squalli", null),
            new Person("Ilyas", "Cherkaoui", null), new Person("Yasmine", "Benkirane", null),
            new Person("Reda", "Belhaj", null), new Person("Sara", "Moussaoui", null),
            new Person("Ayoub", "Haddad", null), new Person("Houda", "Zniber", null),
            new Person("Nabil", "Raji", null), new Person("Wafae", "Essaidi", null),
            new Person("Driss", "Guessous", null));

    private final EmployeeUseCase employees;
    private final EmployeeOnboardingUseCase onboarding;
    private final EmployeeOffboardingUseCase offboarding;
    private final UserDirectoryUseCase users;

    public DevEmployeesSeeder(EmployeeUseCase employees, EmployeeOnboardingUseCase onboarding,
                              EmployeeOffboardingUseCase offboarding, UserDirectoryUseCase users) {
        this.employees = employees;
        this.onboarding = onboarding;
        this.offboarding = offboarding;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!employees.getAll().isEmpty()) {
            log.info("[DEV-SEED] Employees already present, nothing seeded");
            return;
        }
        int created = 0;
        for (int i = 0; i < PEOPLE.size(); i++) {
            int index = i;
            Person person = PEOPLE.get(i);
            String email = person.email() != null ? person.email()
                    : (person.firstName() + "." + person.lastName()).toLowerCase().replace(' ', '-') + "@materia.com";
            if (DevSeed.attempt(log, "employee " + email, () -> {
                EmployeeOutput employee = onboarding.onboard(new OnboardEmployeeInput(
                        null, person.firstName(), person.lastName(), email,
                        String.format("+212 6%02d-%03d%03d", 10 + index, 100 + index * 7, 200 + index * 13),
                        DevSeed.daysAgo(60 + index * 37), statusOf(index), false, null, null));
                if (person.email() != null) {
                    users.findUserByEmail(person.email())
                            .ifPresent(account -> employees.linkUserAccount(employee.id(), account.id()));
                }
                if (index >= PEOPLE.size() - 2) {
                    offboarding.offboard(new OffboardEmployeeInput(employee.id(), DevSeed.daysAgo(index - 20),
                            "End of fixed-term contract", false));
                }
            })) {
                created++;
            }
        }
        log.info("[DEV-SEED] Created {} employees", created);
    }

    /** Mostly active; a few on probation, on leave or suspended (the last two are offboarded afterwards). */
    private static EmploymentStatus statusOf(int index) {
        if (index < 3) return EmploymentStatus.ACTIVE;
        return switch (index % 10) {
            case 4 -> EmploymentStatus.PROBATION;
            case 7 -> EmploymentStatus.ON_LEAVE;
            case 9 -> EmploymentStatus.SUSPENDED;
            default -> EmploymentStatus.ACTIVE;
        };
    }
}
