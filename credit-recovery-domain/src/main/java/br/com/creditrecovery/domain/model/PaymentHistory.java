package br.com.creditrecovery.domain.model;

public record PaymentHistory(
        int paidInstallments,
        int delayedInstallments,
        boolean debtRegularized
) {

    public boolean hasGoodHistory() {
        return paidInstallments >= 6 && delayedInstallments <= 1;
    }
}
