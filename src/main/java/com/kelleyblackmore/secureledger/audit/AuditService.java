package com.kelleyblackmore.secureledger.audit;

import com.kelleyblackmore.secureledger.entity.AuditEvent;
import com.kelleyblackmore.secureledger.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes append-only audit records. Records are written in their own transaction so that
 * an audit failure never silently rolls back business work, and a business rollback never
 * erases the audit trail.
 */
@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String entityType, String entityId, String detail) {
        auditEventRepository.save(new AuditEvent(actor, action, entityType, entityId, detail));
    }
}
