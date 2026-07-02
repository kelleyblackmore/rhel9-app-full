package com.kelleyblackmore.secureledger.service;

import com.kelleyblackmore.secureledger.audit.AuditService;
import com.kelleyblackmore.secureledger.dto.TaskRequest;
import com.kelleyblackmore.secureledger.entity.Task;
import com.kelleyblackmore.secureledger.entity.TaskStatus;
import com.kelleyblackmore.secureledger.exception.ForbiddenOperationException;
import com.kelleyblackmore.secureledger.exception.ResourceNotFoundException;
import com.kelleyblackmore.secureledger.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuditService auditService;

    public TaskService(TaskRepository taskRepository, AuditService auditService) {
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<Task> list(String requester, boolean admin, Pageable pageable) {
        // Admins see everything; users see only their own tasks.
        if (admin) {
            return taskRepository.findAll(pageable);
        }
        return taskRepository.findByOwner(requester, pageable);
    }

    @Transactional(readOnly = true)
    public Task get(Long id, String requester, boolean admin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        ensureCanAccess(task, requester, admin);
        return task;
    }

    @Transactional
    public Task create(TaskRequest request, String requester) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setOwner(requester);
        Task saved = taskRepository.save(task);
        auditService.record(requester, "CREATE", "Task", String.valueOf(saved.getId()),
                "Created task '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public Task update(Long id, TaskRequest request, String requester, boolean admin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        ensureCanAccess(task, requester, admin);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        Task saved = taskRepository.save(task);
        auditService.record(requester, "UPDATE", "Task", String.valueOf(saved.getId()),
                "Updated task '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public void delete(Long id, String requester, boolean admin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        ensureCanAccess(task, requester, admin);
        taskRepository.delete(task);
        auditService.record(requester, "DELETE", "Task", String.valueOf(id),
                "Deleted task '" + task.getTitle() + "'");
    }

    private void ensureCanAccess(Task task, String requester, boolean admin) {
        if (!admin && !task.getOwner().equals(requester)) {
            throw new ForbiddenOperationException("Not the owner of task " + task.getId());
        }
    }
}
