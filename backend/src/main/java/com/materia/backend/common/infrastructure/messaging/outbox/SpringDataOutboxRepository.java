package com.materia.backend.common.infrastructure.messaging.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxMessageJpaEntity, UUID> {

    /** The oldest messages not relayed yet. */
    @Query("select m from OutboxMessageJpaEntity m where m.publishedAt is null order by m.createdAt")
    List<OutboxMessageJpaEntity> findPending(Pageable page);
}
