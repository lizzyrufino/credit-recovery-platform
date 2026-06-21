package br.com.creditrecovery.processor.messaging;

import br.com.creditrecovery.processor.adapter.in.sqs.SqsCreditProfileConsumer;
import br.com.creditrecovery.processor.application.port.in.CreditProfileEventUseCase;
import br.com.creditrecovery.processor.application.service.ProcessingResult;
import br.com.creditrecovery.processor.config.ProcessorProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsCreditProfileConsumerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private CreditProfileEventUseCase eventHandler;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldProcessAndDeleteValidMessage() {
        Message message = Message.builder()
                .messageId("message-1")
                .receiptHandle("receipt-1")
                .body(validMessage())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());
        when(eventHandler.handle(any())).thenReturn(ProcessingResult.PROCESSED);

        consumer().poll();

        verify(eventHandler).handle(any());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldNotDeleteInvalidMessageSoSqsCanRedriveToDlq() {
        Message message = Message.builder()
                .messageId("message-2")
                .receiptHandle("receipt-2")
                .body("{invalid-json")
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(message).build());

        consumer().poll();

        verify(eventHandler, never()).handle(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    private SqsCreditProfileConsumer consumer() {
        return new SqsCreditProfileConsumer(
                sqsClient,
                new ProcessorProperties("CreditRecoveryStrategy", "http://localhost/queue", true, 1, 0),
                objectMapper,
                eventHandler,
                new SimpleMeterRegistry()
        );
    }

    private String validMessage() {
        return """
                {
                  "eventId": "evt-test-001",
                  "correlationId": "corr-test-001",
                  "occurredAt": "2026-06-20T10:00:00Z",
                  "profile": {
                    "document": {"value": "11222333000181"},
                    "daysOverdue": 87,
                    "debtAmount": 125000.50,
                    "products": [{"type": "CREDIT_CARD_PJ", "active": true, "outstandingAmount": 85000.00}],
                    "internalScore": 812,
                    "paymentHistory": {"paidInstallments": 12, "delayedInstallments": 3, "debtRegularized": false},
                    "preferredChannel": "WHATSAPP",
                    "whatsappConsent": true,
                    "riskLevel": "HIGH",
                    "activePjCard": true
                  }
                }
                """;
    }
}
