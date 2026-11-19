package org.example.taskmanager.service;

import org.example.taskmanager.dto.TaskCreateRequestDto;
import org.example.taskmanager.dto.TaskResponseDto;
import org.example.taskmanager.dto.UserDto;
import org.example.taskmanager.entity.TaskEntity;
import org.example.taskmanager.entity.UserEntity;
import org.example.taskmanager.event.TaskAssignedEvent;
import org.example.taskmanager.event.TaskCreatedEvent;
import org.example.taskmanager.exception.TaskNotFoundException;
import org.example.taskmanager.exception.UserNotFoundException;
import org.example.taskmanager.mapper.TaskMapper;
import org.example.taskmanager.model.TaskStatus;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ApplicationEventPublisher applicationEventPublisher, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.taskMapper = taskMapper;
    }

    public TaskResponseDto getTaskById(Long id) {
        logger.info("Getting task {}", id);
        TaskEntity entity = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);

        return taskMapper.toTaskResponseDto(entity);
    }

    public Page<TaskResponseDto> getTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").descending());
        Page<TaskEntity> taskEntityPage = taskRepository.findAll(pageable);

        return taskEntityPage.map(taskMapper::toTaskResponseDto);
    }

    @Transactional
    public TaskResponseDto createTask(TaskCreateRequestDto createRequestDto) {
        logger.info("Creating task: {}", createRequestDto.title());
        TaskEntity entity = taskMapper.toTaskEntity(createRequestDto);
        entity.setStatus(TaskStatus.OPENED);
        TaskEntity savedEntity = taskRepository.save(entity);
        logger.info("Task created with id: {}", savedEntity.getId());

        applicationEventPublisher.publishEvent(new TaskCreatedEvent(savedEntity.getId()));

        return taskMapper.toTaskResponseDto(savedEntity);
    }

    @Transactional
    public TaskResponseDto assignUserToTask(Long id, UserDto userDto) {
        logger.info("Assigning user {} to task with id {}", userDto.name(), id);
        TaskEntity taskEntity = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
        UserEntity userEntity = userRepository.findById(userDto.id()).orElseThrow(UserNotFoundException::new);

        taskEntity.setAssignee(userEntity);
        TaskEntity savedTaskEntity = taskRepository.save(taskEntity);
        logger.info("{} assigned to task with id {}", userDto.name(), id);

        applicationEventPublisher.publishEvent(new TaskAssignedEvent(id, userDto.id()));

        return taskMapper.toTaskResponseDto(savedTaskEntity);
    }

    @Transactional
    public TaskResponseDto changeTaskStatus(Long id, TaskStatus status) {
        logger.info("Applying status {} to task with id {}", status, id);
        TaskEntity taskEntity = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
        taskEntity.setStatus(status);
        TaskEntity savedTaskEntity = taskRepository.save(taskEntity);
        logger.info("Status {} applied to task with id {}", status, id);

        return taskMapper.toTaskResponseDto(savedTaskEntity);
    }
}
