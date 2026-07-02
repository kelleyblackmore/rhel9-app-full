package com.kelleyblackmore.secureledger.repository;

import com.kelleyblackmore.secureledger.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByActor(String actor, Pageable pageable);

    Page<AuditEvent> findByAction(String action, Pageable pageable);

    Page<AuditEvent> findByActorAndAction(String actor, String action, Pageable pageable);
}
