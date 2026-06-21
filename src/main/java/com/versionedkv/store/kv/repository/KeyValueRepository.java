package com.versionedkv.store.kv.repository;

import com.versionedkv.store.kv.repository.custom.KeyValueRepositoryCustom;
import com.versionedkv.store.kv.repository.entity.KeyValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeyValueRepository extends JpaRepository<KeyValueEntity, Long>, KeyValueRepositoryCustom {

    Optional<KeyValueEntity> findByKey(String key);

    List<KeyValueEntity> findByKeyIn(List<String> keys);
}
