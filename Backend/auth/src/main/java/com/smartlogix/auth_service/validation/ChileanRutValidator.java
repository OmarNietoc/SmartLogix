package com.smartlogix.auth_service.validation;

public final class ChileanRutValidator {

    private ChileanRutValidator() {
    }

    public static String normalize(String rut) {
        if (rut == null) {
            throw new IllegalArgumentException("El RUT es obligatorio");
        }

        String normalized = rut.replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase();

        if (!normalized.matches("\\d{7,8}[0-9K]")) {
            throw new IllegalArgumentException("El RUT ingresado no es valido");
        }

        if (!isValidNormalizedRut(normalized)) {
            throw new IllegalArgumentException("El RUT ingresado no es valido");
        }

        return normalized;
    }

    private static boolean isValidNormalizedRut(String rut) {
        String body = rut.substring(0, rut.length() - 1);
        char verifier = rut.charAt(rut.length() - 1);
        int multiplier = 2;
        int sum = 0;

        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }

        int remainder = 11 - (sum % 11);
        char expected;
        if (remainder == 11) {
            expected = '0';
        } else if (remainder == 10) {
            expected = 'K';
        } else {
            expected = Character.forDigit(remainder, 10);
        }

        return verifier == expected;
    }
}
