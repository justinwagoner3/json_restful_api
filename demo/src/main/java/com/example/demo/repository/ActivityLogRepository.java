package com.example.demo.repository;

import com.example.demo.model.ActivityLog;
import com.example.demo.model.ActivityLog.EntityType;
import com.example.demo.model.ActivityLog.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByEntityTypeAndOperation(EntityType entityType, OperationType operation);

    List<ActivityLog> findByEntityTypeAndOperationAndBookId(EntityType entityType, OperationType operation, Integer bookId);
}
