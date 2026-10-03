package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.directory;

import com.materia.backend.contexts.auth.domain.entities.User;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.materia.backend.support.fixtures.UserFixtures.aDisabledReceiver;
import static com.materia.backend.support.fixtures.UserFixtures.aPurchaser;
import static com.materia.backend.support.fixtures.UserFixtures.aReceiver;
import static com.materia.backend.support.fixtures.UserFixtures.anInactiveReceiver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** [T044] Only active, enabled receivers can be listed or assigned (US4-7, US4-9). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthReceiverDirectoryTest {

    @Mock private UserRepository users;

    private AuthReceiverDirectory directory;

    @BeforeEach
    void setUp() {
        directory = new AuthReceiverDirectory(users);
    }

    @Test
    @DisplayName("list: only enabled, active receivers are listed, sorted by name")
    void list_onlyActiveEnabledReceivers_sorted() {
        User zoe = aReceiver().firstName("Zoe").lastName("Z").build();
        User amy = aReceiver().firstName("Amy").lastName("A").build();
        when(users.findByRole(Role.RECEIVER)).thenReturn(List.of(zoe, anInactiveReceiver().build(),
                aDisabledReceiver().build(), amy));

        List<ReceiverDirectory.Receiver> receivers = directory.findAssignableReceivers();

        assertEquals(List.of("Amy A", "Zoe Z"), receivers.stream().map(ReceiverDirectory.Receiver::name).toList());
        assertEquals(amy.getId().toString(), receivers.get(0).id());
        assertEquals(amy.getEmail(), receivers.get(0).email());
    }

    @Test
    @DisplayName("lookup: an active receiver is found by id")
    void lookup_activeReceiver_isFound() {
        User rita = aReceiver().build();
        when(users.findById(rita.getId())).thenReturn(Optional.of(rita));

        assertEquals(rita.getFullName(), directory.findAssignableReceiver(rita.getId().toString()).orElseThrow().name());
    }

    @Test
    @DisplayName("lookup: purchasers, inactive and disabled receivers cannot be assigned (US4-7)")
    void lookup_nonAssignableUsers_areRejected() {
        for (User user : List.of(aPurchaser().build(), anInactiveReceiver().build(), aDisabledReceiver().build())) {
            when(users.findById(user.getId())).thenReturn(Optional.of(user));
            assertTrue(directory.findAssignableReceiver(user.getId().toString()).isEmpty(), user.getEmail());
        }
    }

    @Test
    @DisplayName("lookup: blank, malformed and unknown ids find nobody")
    void lookup_badIds_findNobody() {
        assertTrue(directory.findAssignableReceiver(null).isEmpty());
        assertTrue(directory.findAssignableReceiver("  ").isEmpty());
        assertTrue(directory.findAssignableReceiver("not-a-uuid").isEmpty());
        assertTrue(directory.findAssignableReceiver("00000000-0000-0000-0000-000000000000").isEmpty());
    }

    @Test
    @DisplayName("name: a receiver without a full name is shown by email")
    void name_fallsBackToEmail() {
        User nameless = aReceiver().firstName("").lastName("").build();
        when(users.findByRole(Role.RECEIVER)).thenReturn(List.of(nameless));

        String shown = directory.findAssignableReceivers().get(0).name();

        assertEquals(nameless.getEmail(), shown);
    }
}
