package com.kelleyblackmore.secureledger.controller;

import com.kelleyblackmore.secureledger.dto.AuditEventResponse;
import com.kelleyblackmore.secureledger.entity.AuditEvent;
import com.kelleyblackmore.secureledger.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping
    public Page<AuditEventResponse> list(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditEvent> page;
        boolean hasActor = StringUtils.hasText(actor);
        boolean hasAction = StringUtils.hasText(action);

        if (hasActor && hasAction) {
            page = auditEventRepository.findByActorAndAction(actor, action, pageable);
        } else if (hasActor) {
            page = auditEventRepository.findByActor(actor, pageable);
        } else if (hasAction) {
            page = auditEventRepository.findByAction(action, pageable);
        } else {
            page = auditEventRepository.findAll(pageable);
        }

        return page.map(AuditEventResponse::from);
    }
}
