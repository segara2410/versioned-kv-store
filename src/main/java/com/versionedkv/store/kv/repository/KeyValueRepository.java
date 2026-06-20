package com.versionedkv.store.kv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeyValueRepository extends JpaRepository<KeyValueEntity, Long> {

    Optional<KeyValueEntity> findByKey(String key);
}
