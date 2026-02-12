package com.smartstay.reports.utils;

public class AmountToWordsUtils {
    private static final String[] units = {
            "", "One", "Two", "Three", "Four", "Five", "Six",
            "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
            "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty",
            "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(double amount) {
        long rupees = (long) amount;

        return convertRupees(rupees);
    }

    private static String convertRupees(long number) {
        if (number == 0) return "Zero Rupees";

        StringBuilder result = new StringBuilder();

        if (number / 10000000 > 0) {
            result.append(convertRupees(number / 10000000))
                    .append(" Crore ");
            number %= 10000000;
        }

        if (number / 100000 > 0) {
            result.append(convertRupees(number / 100000))
                    .append(" Lakh ");
            number %= 100000;
        }

        if (number / 1000 > 0) {
            result.append(convertRupees(number / 1000))
                    .append(" Thousand ");
            number %= 1000;
        }

        if (number / 100 > 0) {
            result.append(units[(int) (number / 100)])
                    .append(" Hundred ");
            number %= 100;
        }

        if (number > 0) {
            result.append(convertTwoDigits((int) number));
        }

        return result.toString().trim() + " Rupees";
    }

    private static String convertTwoDigits(int number) {
        if (number < 20)
            return units[number];
        else
            return tens[number / 10] +
                    ((number % 10 > 0) ? " " + units[number % 10] : "");
    }
}
