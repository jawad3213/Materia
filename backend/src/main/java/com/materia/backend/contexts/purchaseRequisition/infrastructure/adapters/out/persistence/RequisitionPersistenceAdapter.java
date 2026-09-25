package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionSearchFilter;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.mappers.RequisitionPersistenceMapper;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.repositories.SpringDataRequisitionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter for purchase requisitions.
 */
@Component
public class RequisitionPersistenceAdapter implements RequisitionRepository {

    private final SpringDataRequisitionRepository jpaRepository;
    private final RequisitionPersistenceMapper mapper;

    public RequisitionPersistenceAdapter(SpringDataRequisitionRepository jpaRepository,
                                         RequisitionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Requisition> findById(UUID uuid) {
        return jpaRepository.findById(uuid).map(mapper::toDomainEntity);
    }

    @Override
    public List<Requisition> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Requisition save(Requisition entity) {
        if (entity.getId() != null) {
            Optional<RequisitionJpaEntity> existingOpt = jpaRepository.findById(entity.getId());
            if (existingOpt.isPresent()) {
                RequisitionJpaEntity existing = existingOpt.get();
                mapper.updateJpaEntity(existing, entity);
                return mapper.toDomainEntity(jpaRepository.save(existing));
            }
        }
        RequisitionJpaEntity jpa = mapper.toJpaEntity(entity);
        return mapper.toDomainEntity(jpaRepository.save(jpa));
    }

    @Override
    public List<Requisition> saveAll(List<Requisition> entities) {
        List<RequisitionJpaEntity> jpaEntities = entities.stream()
                .map(mapper::toJpaEntity)
                .collect(Collectors.toList());
        return jpaRepository.saveAll(jpaEntities).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public void delete(Requisition entity) {
        jpaRepository.deleteById(entity.getId());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Requisition> findAllById(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Requisition> findByCode(String code) {
        return jpaRepository.findByRequisitionCode(code).map(mapper::toDomainEntity);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByRequisitionCode(code);
    }

    @Override
    public List<Requisition> findByStatus(RequisitionStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Requisition> findByRequesterId(String requesterId) {
        return jpaRepository.findByRequesterId(requesterId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Requisition> findFirstByRequesterName(String requesterName) {
        if (requesterName == null || requesterName.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findFirstByRequesterNameIgnoreCaseOrderByCreatedAtDesc(requesterName.trim())
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<Requisition> findByApproverId(String approverId) {
        return jpaRepository.findByApproverId(approverId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Requisition> findByRequiredDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByRequiredDateBetween(startDate, endDate).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Requisition> findBySubmittedDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findBySubmittedDateBetween(startDate, endDate).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Requisition> search(String keyword) {
        return jpaRepository.findAll(buildSearchSpecification(keyword)).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<Requisition> searchAdvanced(RequisitionSearchFilter filter, int page, int size) {
        Specification<RequisitionJpaEntity> specification = Specification.where(buildSearchSpecification(filter.getKeyword()))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (StringUtils.hasText(filter.getRequesterId())) {
                        predicates.add(cb.equal(root.get("requesterId"), filter.getRequesterId()));
                    }
                    if (StringUtils.hasText(filter.getApproverId())) {
                        predicates.add(cb.equal(root.get("approverId"), filter.getApproverId()));
                    }
                    if (filter.getStatus() != null) {
                        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                    }
                    if (StringUtils.hasText(filter.getCurrencyCode())) {
                        predicates.add(cb.equal(root.get("currencyCode"), filter.getCurrencyCode()));
                    }
                    if (filter.getRequiredDateFrom() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("requiredDate"), filter.getRequiredDateFrom()));
                    }
                    if (filter.getRequiredDateTo() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("requiredDate"), filter.getRequiredDateTo()));
                    }
                    if (filter.getSubmittedDateFrom() != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("submittedDate"), filter.getSubmittedDateFrom()));
                    }
                    if (filter.getSubmittedDateTo() != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("submittedDate"), filter.getSubmittedDateTo()));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                });

        Page<RequisitionJpaEntity> jpaPage = jpaRepository.findAll(specification, PageRequest.of(page, size));
        List<Requisition> content = jpaPage.getContent().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages(),
                jpaPage.isLast()
        );
    }

    private Specification<RequisitionJpaEntity> buildSearchSpecification(String keywordString) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keywordString)) {
                return cb.conjunction();
            }

            String[] keywords = keywordString.split("[,\\s]+");
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String kw : keywords) {
                if (!StringUtils.hasText(kw)) {
                    continue;
                }

                String searchPattern = "%" + kw.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("requisitionCode")), searchPattern);
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descriptionMatch = cb.like(cb.lower(cb.coalesce(root.get("description"), "")), searchPattern);
                Predicate justificationMatch = cb.like(cb.lower(cb.coalesce(root.get("justification"), "")), searchPattern);
                Predicate requesterNameMatch = cb.like(cb.lower(cb.coalesce(root.get("requesterName"), "")), searchPattern);

                keywordPredicates.add(cb.or(codeMatch, titleMatch, descriptionMatch, justificationMatch, requesterNameMatch));
            }

            return cb.and(keywordPredicates.toArray(new Predicate[0]));
        };
    }
}
