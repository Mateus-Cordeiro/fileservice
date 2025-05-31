package com.github.mateuscordeiro.fileservice.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;

import org.junit.jupiter.api.Test;

public class ValidationUtilsTest {
    // validateWritablePath
    @Test
    void validateWritablePath_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.validateWritablePath("some/path.txt"));
    }

    @Test
    void validateWritablePath_whenNull_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> ValidationUtils.validateWritablePath(null));
    }

    @Test
    void validateWritablePath_whenBlank_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> ValidationUtils.validateWritablePath("  "));
    }

    // requireNonBlank
    @Test
    void requireNonBlank_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonBlank("data", "field"));
    }

    @Test
    void requireNonBlank_whenNull_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank(null, "testField"));
        assertTrue(ex.getMessage().contains("testField"));
    }

    @Test
    void requireNonBlank_whenBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonBlank("   ", "anotherField"));
    }

    // requireNonNull
    @Test
    void requireNonNull_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNull(new Object(), "field"));
    }

    @Test
    void requireNonNull_whenNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonNull(null, "myField"));
    }

    // requirePositive
    @Test
    void requirePositive_whenPositive_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requirePositive(5, "value"));
    }

    @Test
    void requirePositive_whenZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requirePositive(0, "value"));
    }

    @Test
    void requirePositive_whenNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requirePositive(-1, "value"));
    }

    // requireNonNegative
    @Test
    void requireNonNegative_whenPositive_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNegative(3, "value"));
    }

    @Test
    void requireNonNegative_whenZero_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonNegative(0, "value"));
    }

    @Test
    void requireNonNegative_whenNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonNegative(-10, "value"));
    }
}