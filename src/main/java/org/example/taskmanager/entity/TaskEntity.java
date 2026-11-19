package org.example.taskmanager.entity;

import jakarta.persistence.*;
import org.example.taskmanager.model.TaskStatus;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_assignee", columnList = "users_id"),
        @Index(name = "idx_task_title", columnList = "title")
})
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @ManyToOne
    @JoinColumn(name = "users_id")
    private UserEntity assignee;
    private String description;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public TaskEntity(Long id, String title, UserEntity assignee, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.assignee = assignee;
        this.description = description;
        this.status = status;
    }

    public TaskEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(UserEntity assignee) {
        this.assignee = assignee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
