package br.com.creditrecovery.api.validation;

import br.com.creditrecovery.domain.model.CustomerDocument;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return CustomerDocument.isValidCnpj(value);
    }
}
