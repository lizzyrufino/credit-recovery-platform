package br.com.creditrecovery.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerDocumentTest {

    @Test
    void shouldValidateCnpjWithDigits() {
        assertTrue(CustomerDocument.isValidCnpj("11222333000181"));
    }

    @Test
    void shouldNormalizeFormattedCnpj() {
        CustomerDocument document = CustomerDocument.of("11.222.333/0001-81");

        assertEquals("11222333000181", document.value());
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertFalse(CustomerDocument.isValidCnpj("11111111111111"));
        assertThrows(IllegalArgumentException.class, () -> CustomerDocument.of("123"));
    }
}
