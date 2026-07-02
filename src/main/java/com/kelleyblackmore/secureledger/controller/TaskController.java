package com.kelleyblackmore.secureledger.controller;

import com.kelleyblackmore.secureledger.dto.TaskRequest;
import com.kelleyblackmore.secureledger.dto.TaskResponse;
import com.kelleyblackmore.secureledger.entity.Task;
import com.kelleyblackmore.secureledger.security.SecurityUtils;
import com.kelleyblackmore.secureledger.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Page<TaskResponse> list(Authentication authentication,
                                   @PageableDefault(size = 20) Pageable pageable) {
        boolean admin = SecurityUtils.isAdmin(authentication);
        return taskService.list(authentication.getName(), admin, pageable)
                .map(TaskResponse::from);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, Authentication authentication) {
        boolean admin = SecurityUtils.isAdmin(authentication);
        Task task = taskService.get(id, authentication.getName(), admin);
        return TaskResponse.from(task);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               Authentication authentication) {
        Task task = taskService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody TaskRequest request,
                               Authentication authentication) {
        boolean admin = SecurityUtils.isAdmin(authentication);
        Task task = taskService.update(id, request, authentication.getName(), admin);
        return TaskResponse.from(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        boolean admin = SecurityUtils.isAdmin(authentication);
        taskService.delete(id, authentication.getName(), admin);
        return ResponseEntity.noContent().build();
    }
}
