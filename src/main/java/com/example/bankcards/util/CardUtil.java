package com.example.bankcards.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

public class CardUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/yy");
    private static final Random RANDOM = new Random();

    // Пример BIN (Bank Identification Number) — VISA: 400000
    private static final String BIN = "400000";

    /**
     * Маскирует номер карты или CVV
     * @param value строка с номером карты / CVV
     * @return строку вида "**** **** **** 1234"
     */
    public static String mask(String value) {
        if (value == null || value.isBlank()) return null;

        if (value.length() > 4) {
            // Маскировка номера карты
            return "**** **** **** " + value.substring(value.length() - 4);
        } else {
            // Маскировка CVV
            return "*".repeat(value.length());
        }
    }

    /**
     * Преобразует строку формата "MM/yy" в LocalDate
     * (например, "05/23" → 2023-05-31).
     */
    public static LocalDate parseExpiryDate(String dateStr) {
        try {
            YearMonth yearMonth = YearMonth.parse(dateStr, FORMATTER);
            return yearMonth.atEndOfMonth();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Expected MM/yy", e);
        }
    }

    /**
     * Преобразует LocalDate в строку формата "MM/yy"
     * (например, 2023-05-31 → "05/23").
     */
    public static String formatExpiryDate(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        return yearMonth.format(FORMATTER);
    }

    /**
     * Генерирует валидный номер банковской карты (16 цифр) с контрольной по алгоритму Луна.
     */
    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(BIN);

        // Случайные цифры до 15 символа (всего 15, потому что 16-я будет контрольной)
        while (cardNumber.length() < 15) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        // Расчёт контрольной цифры по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    /**
     * Вычисляет контрольную цифру по алгоритму Луна.
     */
    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Character.getNumericValue(number.charAt(number.length() - 1 - i));
            if (i % 2 == 0) { // Чётные позиции справа (не индексы!)
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Преобразует сумму в копейках/центах в десятичное представление (рубли/доллары) с двумя знаками после запятой.
     *
     * @param amountInCents сумма в самых мелких единицах (например, 123456 = 1234.56)
     * @return BigDecimal с двумя знаками после запятой
     */
    public static BigDecimal fromCentsToDecimal(BigInteger amountInCents) {
        if (amountInCents == null) {
            return null;
        }
        return new BigDecimal(amountInCents).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Преобразует сумму из числа с двумя знаками после запятой в
     * представление BifInteger
     *
     * @param amount сумма с двумя знаками после запятой
     * @return
     */
    public static BigInteger getAmountAsBigInteger(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact();
    }

    // Тест
    public static void main(String[] args) {
        String cardNumber = generateCardNumber();
        System.out.println("Generated Card Number: " + cardNumber);
    }
}