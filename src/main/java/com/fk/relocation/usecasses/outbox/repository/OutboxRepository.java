package com.fk.relocation.usecasses.outbox.repository;

import com.fk.relocation.OutboxStatus;
import com.fk.relocation.usecasses.outbox.model.Outbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Outbox o WHERE o.status IN :statuses ORDER BY o.id ASC")
    List<Outbox> findAndLockBatch(@Param("statuses") List<OutboxStatus> statuses, Pageable pageable);
}
