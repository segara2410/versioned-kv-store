package com.versionedkv.store.kv.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "key_value_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uni_kv_versions_key_version", columnNames = {"key", "version"})
})
@Data
@NoArgsConstructor
public class KeyValueVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public KeyValueVersionEntity(String key, String value, Long version) {
        this.key = key;
        this.value = value;
        this.version = version;
    }
}
