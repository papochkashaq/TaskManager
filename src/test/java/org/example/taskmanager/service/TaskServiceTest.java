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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    public static final Long TASK_ID = 1L;
    public static final Long NON_EXISTENT_TASK_ID = -1L;
    public static final String TASK_NAME = "TASK_NAME";
    public static final String TASK_DESCRIPTION = "TASK_DESCRIPTION";

    public static final Long USER_ID = 2L;
    public static final Long NON_EXISTENT_USER_ID = -2L;
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_EMAIL = "user@example.com";

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private TaskMapper taskMapper;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository, applicationEventPublisher, taskMapper);
    }

    @Test
    void shouldReturnTaskById() {
        TaskEntity entity = taskEntity();
        TaskResponseDto expected = taskResponseDto(TaskStatus.OPENED);

        when(taskRepository.findById(TASK_ID))
                .thenReturn(Optional.of(entity));
        when(taskMapper.toTaskResponseDto(entity))
                .thenReturn(expected);

        TaskResponseDto actual = taskService.getTaskById(TASK_ID);

        assertEquals(expected, actual);
        verify(taskRepository).findById(TASK_ID);
        verify(taskMapper).toTaskResponseDto(entity);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentTask() {
        when(taskRepository.findById(NON_EXISTENT_TASK_ID)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(NON_EXISTENT_TASK_ID));
        verify(taskRepository).findById(NON_EXISTENT_TASK_ID);
        verifyNoInteractions(taskMapper, applicationEventPublisher);
    }

    @Test
    void shouldReturnTasksPage() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").descending());
        TaskEntity entity = taskEntity();
        TaskResponseDto responseDto = taskResponseDto(TaskStatus.OPENED);

        Page<TaskEntity> entityPage = new PageImpl<>(
                List.of(entity),
                pageable,
                6
        );

        when(taskRepository.findAll(pageable))
                .thenReturn(entityPage);
        when(taskMapper.toTaskResponseDto(entity))
                .thenReturn(responseDto);

        Page<TaskResponseDto> actual = taskService.getTasks(0, 5);

        assertEquals(List.of(responseDto), actual.getContent());
        assertEquals(6, actual.getTotalElements());
        assertEquals(2, actual.getTotalPages());
        assertEquals(0, actual.getNumber());
        assertEquals(5, actual.getSize());
        verify(taskRepository).findAll(pageable);
        verify(taskMapper).toTaskResponseDto(entity);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldReturnEmptyTasksPage() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").descending());

        when(taskRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<TaskResponseDto> actual = taskService.getTasks(0, 5);

        assertTrue(actual.isEmpty());
        assertEquals(0, actual.getTotalElements());
        verify(taskRepository).findAll(pageable);
        verifyNoInteractions(taskMapper, applicationEventPublisher);
    }

    @Test
    void shouldCreateTaskWithOpenedStatus() {
        TaskCreateRequestDto request = taskCreateRequestDto();
        TaskEntity mappedEntity = taskEntity();
        TaskEntity savedEntity = taskEntity();
        savedEntity.setId(TASK_ID);
        TaskResponseDto expected = taskResponseDto(TaskStatus.OPENED);

        when(taskMapper.toTaskEntity(request))
                .thenReturn(mappedEntity);
        when(taskRepository.save(mappedEntity))
                .thenReturn(savedEntity);
        when(taskMapper.toTaskResponseDto(savedEntity))
                .thenReturn(expected);

        TaskResponseDto actual = taskService.createTask(request);

        assertEquals(expected, actual);
        assertEquals(TaskStatus.OPENED, mappedEntity.getStatus());
        verify(taskMapper).toTaskEntity(request);
        verify(taskRepository).save(mappedEntity);
        verify(taskMapper).toTaskResponseDto(savedEntity);
        verify(applicationEventPublisher).publishEvent(new TaskCreatedEvent(TASK_ID));
    }

    @Test
    void shouldAssignUserToTask() {
        TaskEntity existingEntity = taskEntity();
        TaskEntity savedEntity = taskEntity();

        UserDto userDto = userDto(USER_ID);
        UserEntity userEntity = userEntity();
        userEntity.setId(USER_ID);

        TaskResponseDto expected = taskResponseDto(TaskStatus.OPENED);

        when(taskRepository.findById(TASK_ID))
                .thenReturn(Optional.of(existingEntity));
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(userEntity));
        when(taskRepository.save(existingEntity))
                .thenReturn(savedEntity);
        when(taskMapper.toTaskResponseDto(savedEntity))
                .thenReturn(expected);

        TaskResponseDto actual =
                taskService.assignUserToTask(TASK_ID, userDto);

        assertEquals(expected, actual);
        assertSame(userEntity, existingEntity.getAssignee());
        verify(taskRepository).findById(TASK_ID);
        verify(userRepository).findById(USER_ID);
        verify(taskRepository).save(existingEntity);
        verify(applicationEventPublisher).publishEvent(new TaskAssignedEvent(TASK_ID, USER_ID));
        verify(taskMapper).toTaskResponseDto(savedEntity);
    }

    @Test
    void shouldThrowExceptionWhenAssigningUserToNonExistedTask() {
        UserDto userDto = userDto(USER_ID);
        when(taskRepository.findById(NON_EXISTENT_TASK_ID)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.assignUserToTask(NON_EXISTENT_TASK_ID, userDto));
        verify(taskRepository).findById(NON_EXISTENT_TASK_ID);
        verifyNoInteractions(taskMapper, applicationEventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenAssigningNonExistedUserToTask() {
        TaskEntity taskEntity = taskEntity();
        UserDto userDto = userDto(NON_EXISTENT_USER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(taskEntity));
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> taskService.assignUserToTask(TASK_ID, userDto));
        verify(taskRepository).findById(TASK_ID);
        verify(userRepository).findById(NON_EXISTENT_USER_ID);
        verifyNoInteractions(taskMapper, applicationEventPublisher);
    }

    @Test
    void shouldChangeTaskStatus() {
        TaskEntity existingEntity = taskEntity();
        TaskEntity savedEntity = taskEntity();

        TaskResponseDto expected = taskResponseDto(TaskStatus.BACKLOG);

        when(taskRepository.findById(TASK_ID))
                .thenReturn(Optional.of(existingEntity));
        when(taskRepository.save(existingEntity))
                .thenReturn(savedEntity);
        when(taskMapper.toTaskResponseDto(savedEntity))
                .thenReturn(expected);

        TaskResponseDto actual =
                taskService.changeTaskStatus(TASK_ID, TaskStatus.BACKLOG);

        assertEquals(expected, actual);
        assertEquals(TaskStatus.BACKLOG, existingEntity.getStatus());
        verify(taskRepository).findById(TASK_ID);
        verify(taskRepository).save(existingEntity);
        verify(taskMapper).toTaskResponseDto(savedEntity);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenChangeStatusToNonExistedTask() {
        when(taskRepository.findById(NON_EXISTENT_TASK_ID)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.changeTaskStatus(NON_EXISTENT_TASK_ID, TaskStatus.CLOSED));
        verify(taskRepository).findById(NON_EXISTENT_TASK_ID);
        verifyNoInteractions(taskMapper, applicationEventPublisher);
    }

    private UserEntity userEntity() {
        return new UserEntity();
    }

    private TaskEntity taskEntity() {
        return new TaskEntity();
    }

    private UserDto userDto(Long id) {
        return new UserDto(
                id,
                USER_NAME,
                USER_EMAIL
        );
    }

    private TaskResponseDto taskResponseDto(TaskStatus status) {
        return new TaskResponseDto(
                TASK_ID,
                TASK_NAME,
                userDto(USER_ID),
                TASK_DESCRIPTION,
                status
        );
    }

    private TaskCreateRequestDto taskCreateRequestDto() {
        return new TaskCreateRequestDto(
                TASK_NAME,
                TASK_DESCRIPTION
        );
    }
}
