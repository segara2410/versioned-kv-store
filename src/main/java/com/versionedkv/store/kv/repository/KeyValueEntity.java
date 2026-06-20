package com.versionedkv.store.kv.repository;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "key_values")
@Data
@NoArgsConstructor
public class KeyValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false, length = 4000)
    private String value;

    public KeyValueEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
