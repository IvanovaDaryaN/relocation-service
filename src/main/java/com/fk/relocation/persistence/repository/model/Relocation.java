package com.fk.relocation.persistence.repository.model;

import com.fk.relocation.RelocationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "relocations", schema = "relocation_service")
public class Relocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "surname", length = 50, nullable = false)
    private String surname;

    @Column(nullable = false)
    private Long countryId;

    @Column(nullable = false)
    private String cvUuid;

    @Column(nullable = false)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    private RelocationStatus status;
}
