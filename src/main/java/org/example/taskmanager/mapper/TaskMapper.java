package org.example.taskmanager.mapper;

import org.example.taskmanager.dto.TaskCreateRequestDto;
import org.example.taskmanager.dto.TaskResponseDto;
import org.example.taskmanager.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    TaskEntity toTaskEntity(TaskResponseDto taskResponseDto);

    TaskEntity toTaskEntity(TaskCreateRequestDto createRequestDto);

    TaskResponseDto toTaskResponseDto(TaskEntity taskEntity);
}
