package com.fk.relocation.persistence.repository;

import com.fk.relocation.persistence.repository.model.Relocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelocationRepository extends JpaRepository<Relocation, Long> {}
