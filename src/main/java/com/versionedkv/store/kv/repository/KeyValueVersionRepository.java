package com.versionedkv.store.kv.repository;

import com.versionedkv.store.kv.repository.entity.KeyValueVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface KeyValueVersionRepository extends JpaRepository<KeyValueVersionEntity, Long> {

    Optional<KeyValueVersionEntity> findTopByKeyAndCreatedAtLessThanEqualOrderByCreatedAtDesc(String key, Instant timestamp);
}
