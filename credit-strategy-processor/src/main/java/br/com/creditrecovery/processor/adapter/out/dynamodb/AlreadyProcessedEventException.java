package br.com.creditrecovery.processor.adapter.out.dynamodb;

public class AlreadyProcessedEventException extends RuntimeException {

    public AlreadyProcessedEventException(String eventId) {
        super("Event already processed: " + eventId);
    }
}
