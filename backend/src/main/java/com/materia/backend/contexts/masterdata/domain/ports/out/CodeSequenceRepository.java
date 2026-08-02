package com.materia.backend.contexts.masterdata.domain.ports.out;

/**
 * Output port for managing code sequences.
 * This guarantees atomic and conflict-free sequence generation.
 */
public interface CodeSequenceRepository {

    /**
     * Gets the next sequence value for the given prefix and increments it atomically.
     * @param prefix The prefix (e.g., "RAW", "SUP").
     * @return The next available number.
     */
    int getNextValueAndIncrement(String prefix);
}
