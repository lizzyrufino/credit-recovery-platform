package br.com.creditrecovery.processor.adapter.in.sqs;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.processor.application.port.in.CreditProfileEventUseCase;
import br.com.creditrecovery.processor.application.service.ProcessingResult;
import br.com.creditrecovery.processor.config.ProcessorProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsCreditProfileConsumer {

    private final SqsClient sqsClient;
    private final ProcessorProperties properties;
    private final ObjectMapper objectMapper;
    private final CreditProfileEventUseCase eventUseCase;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${app.processor.poll-delay-ms:2000}")
    public void poll() {
        if (!properties.pollingEnabled() || properties.queueUrl() == null || properties.queueUrl().isBlank()) {
            return;
        }

        sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(properties.queueUrl())
                        .maxNumberOfMessages(properties.maxMessages())
                        .waitTimeSeconds(properties.waitTimeSeconds())
                        .messageAttributeNames("All")
                        .build())
                .messages()
                .forEach(this::processMessage);
    }

    private void processMessage(Message message) {
        try {
            CreditProfileReceivedEvent event = objectMapper.readValue(message.body(), CreditProfileReceivedEvent.class);
            MDC.put("correlationId", event.correlationId());

            ProcessingResult result = eventUseCase.handle(event);
            deleteMessage(message);
            meterRegistry.counter("sqs.message.processed.count", "result", result.name()).increment();
            log.info("sqs_message_processed messageId={} eventId={} result={}", message.messageId(), event.eventId(), result);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            meterRegistry.counter("sqs.message.failed.count").increment();
            log.warn("invalid_sqs_message_not_deleted messageId={} error={}", message.messageId(), exception.getClass().getSimpleName());
        } catch (Exception exception) {
            meterRegistry.counter("sqs.message.failed.count").increment();
            log.error("sqs_message_processing_failed messageId={}", message.messageId(), exception);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .receiptHandle(message.receiptHandle())
                .build());
    }
}
