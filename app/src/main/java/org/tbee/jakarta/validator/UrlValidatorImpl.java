package org.tbee.jakarta.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlValidatorImpl implements ConstraintValidator<UrlValidator, String> {

    public void initialize(UrlValidator constraintAnnotation) {
    }

    public boolean isValid(String urlString, ConstraintValidatorContext constraintContext) {
        if (urlString == null) {
            return true;
        }

        try {
            new URL(urlString).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    public static boolean isValid(String urlString) {
        return new UrlValidatorImpl().isValid(urlString, null);
    }
}