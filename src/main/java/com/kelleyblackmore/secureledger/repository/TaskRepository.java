package com.kelleyblackmore.secureledger.repository;

import com.kelleyblackmore.secureledger.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwner(String owner, Pageable pageable);
}
