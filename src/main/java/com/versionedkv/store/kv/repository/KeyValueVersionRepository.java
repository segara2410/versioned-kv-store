package com.versionedkv.store.kv.repository;

import com.versionedkv.store.kv.repository.entity.KeyValueVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyValueVersionRepository extends JpaRepository<KeyValueVersionEntity, Long> {
}
