package br.com.creditrecovery.processor.application.service;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.StrategyStatus;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class RecoveryStrategyEngine {

    private final List<RecoveryRule> rules;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    public RecoveryStrategyEngine(List<RecoveryRule> rules, MeterRegistry meterRegistry, Clock clock) {
        this.rules = List.copyOf(rules);
        this.meterRegistry = meterRegistry;
        this.clock = clock;
    }

    public RecoveryStrategy generate(CustomerCreditProfile profile) {
        List<RecoveryAction> actions = rules.stream()
                .peek(rule -> meterRegistry.counter("strategy.rules.executed.count").increment())
                .filter(rule -> rule.appliesTo(profile))
                .map(rule -> rule.evaluate(profile))
                .sorted(Comparator.comparingInt(action -> priorityWeight(action.priority())))
                .toList();

        return new RecoveryStrategy(
                profile.document(),
                profile.riskLevel(),
                profile.daysOverdue(),
                profile.debtAmount(),
                profile.primaryProductType(),
                profile.internalScore(),
                actions,
                partnerOfficeFor(profile),
                "v1",
                StrategyStatus.ACTIVE,
                Instant.now(clock)
        );
    }

    private int priorityWeight(ActionPriority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private String partnerOfficeFor(CustomerCreditProfile profile) {
        if (profile.daysOverdue() > 90) {
            return profile.debtAmount().longValue() >= 100_000 ? "OFFICE_SPECIALIZED_HIGH_VALUE" : "OFFICE_A";
        }
        return "";
    }
}
