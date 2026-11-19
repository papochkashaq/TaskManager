package org.example.taskmanager.kafka;

import org.example.taskmanager.event.TaskAssignedEvent;
import org.example.taskmanager.event.TaskCreatedEvent;
import org.example.taskmanager.event.TaskEvent;
import org.example.taskmanager.model.TaskEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisher.class);

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final String topic;

    public TaskEventPublisher(
            KafkaTemplate<String, TaskEvent> kafkaTemplate,
            @Value("${app.kafka.task-events-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskCreatedEvent event) {
        log.info("Handling TaskCreatedEvent for taskId: {}", event.taskId());

        TaskEvent kafkaEvent = new TaskEvent(
                UUID.randomUUID(),
                TaskEventType.TASK_CREATED,
                event.taskId(),
                null,
                LocalDateTime.now()
        );

        publishToKafka(kafkaEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskAssigned(TaskAssignedEvent event) {
        log.info("Handling TaskAssignedEvent for taskId: {}, assigneeId: {}",
                event.taskId(), event.assigneeId());

        TaskEvent kafkaEvent = new TaskEvent(
                UUID.randomUUID(),
                TaskEventType.TASK_ASSIGNED,
                event.taskId(),
                event.assigneeId(),
                LocalDateTime.now()
        );

        publishToKafka(kafkaEvent);
    }

    private void publishToKafka(TaskEvent event) {
        String key = event.taskId().toString();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish task event: {}", event, exception);
                        return;
                    }

                    var metadata = result.getRecordMetadata();
                    log.info(
                            "Task event published: taskId={}, partition={}, offset={}",
                            event.taskId(),
                            metadata.partition(),
                            metadata.offset()
                    );
                });
    }
}
