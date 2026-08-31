package com.clyvo.veterinary.tutor.infrastructure.persistence.adapter;

import com.clyvo.veterinary.tutor.domain.model.Tutor;
import com.clyvo.veterinary.tutor.domain.repository.TutorRepository;
import com.clyvo.veterinary.tutor.infrastructure.persistence.entity.TutorEntity;
import com.clyvo.veterinary.tutor.infrastructure.persistence.mapper.TutorEntityMapper;
import com.clyvo.veterinary.tutor.infrastructure.persistence.repository.TutorJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TutorRepositoryAdapter implements TutorRepository {

    private final TutorJpaRepository jpaRepository;
    private final TutorEntityMapper mapper;

    public TutorRepositoryAdapter(TutorJpaRepository jpaRepository, TutorEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Tutor save(Tutor tutor) {
        TutorEntity entity = mapper.toEntity(tutor);
        TutorEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Tutor> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Tutor> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Tutor> findByDocument(String document) {
        return jpaRepository.findByDocument(document).map(mapper::toDomain);
    }

    @Override
    public boolean existsByDocument(String document) {
        return jpaRepository.existsByDocument(document);
    }

    @Override
    public List<Tutor> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
