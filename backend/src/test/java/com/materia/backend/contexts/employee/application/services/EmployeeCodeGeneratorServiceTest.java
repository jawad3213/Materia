package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * [T082] Employee code generator: format contract and concurrency guard (US4).
 *
 * <p>The concurrency test uses a mocked sequence repository that returns distinct
 * values (0..N) so it stays deterministic. What it verifies is that the generator
 * delegates the atomicity concern entirely to the {@link CodeSequenceRepository},
 * returning a different, valid code for every call.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeCodeGeneratorServiceTest {

    @Mock
    private CodeSequenceRepository sequenceRepository;

    private EmployeeCodeGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        generatorService = new EmployeeCodeGeneratorService(sequenceRepository);
    }

    // ---- Format contract ----

    @Test
    @DisplayName("generateCode: returns an EMP-NNNN formatted code")
    void generateCode_format() {
        when(sequenceRepository.getNextValueAndIncrement(eq("EMP"))).thenReturn(1);

        EmployeeCode code = generatorService.generateCode();

        assertEquals("EMP-0001", code.getValue());
    }

    @Test
    @DisplayName("generateCode: delegates all atomicity to the sequence repository")
    void generateCode_delegatesToSequenceRepository() {
        when(sequenceRepository.getNextValueAndIncrement("EMP")).thenReturn(42);

        EmployeeCode code = generatorService.generateCode();

        verify(sequenceRepository).getNextValueAndIncrement("EMP");
        assertEquals("EMP-0042", code.getValue());
    }

    @Test
    @DisplayName("generateCode: zero-pads to at least four digits")
    void generateCode_padsSmallNumbers() {
        when(sequenceRepository.getNextValueAndIncrement(any())).thenReturn(7);

        assertEquals("EMP-0007", generatorService.generateCode().getValue());
    }

    @Test
    @DisplayName("generateCode: handles large sequence numbers beyond 4 digits")
    void generateCode_handlesLargeNumbers() {
        when(sequenceRepository.getNextValueAndIncrement(any())).thenReturn(99999);

        assertEquals("EMP-99999", generatorService.generateCode().getValue());
    }

    // ---- Concurrency ----

    @RepeatedTest(3)
    @DisplayName("generateCode: under concurrent load each call receives a unique sequence number")
    void generateCode_concurrency_producesUniqueValues() throws Exception {
        int threads = 20;
        // Sequence repository hands out 1..N, simulating the DB row-lock guarantee.
        final int[] counter = {0};
        when(sequenceRepository.getNextValueAndIncrement("EMP"))
                .thenAnswer(inv -> ++counter[0]);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> generatorService.generateCode().getValue());
        }

        List<Future<String>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        List<String> codes = new ArrayList<>();
        for (Future<String> f : futures) {
            codes.add(f.get());
        }

        // All codes must be distinct — the sequence repository is the uniqueness source.
        assertEquals(threads, codes.stream().distinct().count(),
                "Every concurrent call must produce a different code");
    }
}
