package br.com.creditrecovery.domain.model;

import java.util.Objects;

public record CustomerDocument(String value) {

    public CustomerDocument {
        value = normalize(value);
        if (!isValidCnpj(value)) {
            throw new IllegalArgumentException("Invalid CNPJ");
        }
    }

    public static CustomerDocument of(String rawValue) {
        return new CustomerDocument(rawValue);
    }

    public static String normalize(String rawValue) {
        return Objects.requireNonNull(rawValue, "CNPJ is required").replaceAll("\\D", "");
    }

    public static boolean isValidCnpj(String rawValue) {
        if (rawValue == null) {
            return false;
        }

        String cnpj = normalize(rawValue);
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(cnpj.substring(0, 12), new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int secondDigit = calculateDigit(cnpj.substring(0, 12) + firstDigit, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        return cnpj.equals(cnpj.substring(0, 12) + firstDigit + secondDigit);
    }

    public String hashed() {
        return Integer.toHexString(value.hashCode());
    }

    private static int calculateDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(base.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
