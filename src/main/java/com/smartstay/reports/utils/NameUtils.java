package com.smartstay.reports.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NameUtils {

    public static String getFullName(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();
        if (firstName != null) {
            builder.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            builder.append(" ");
            builder.append(lastName);
        }

        return builder.toString();
    }

    public static String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();

        if (firstName != null) {
            initials.append(firstName.toUpperCase().charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.toUpperCase().charAt(0));
        }
        else {
            String[] fnameArr = firstName.split(" ");
            if (fnameArr.length > 1) {
                String lastString = fnameArr[fnameArr.length - 1];
                initials.append(lastString.toUpperCase().charAt(0));
            }
            else {
                if (firstName.length() > 1) {
                    initials.append(firstName.toUpperCase().charAt(1));
                }
                else {
                    initials.append(firstName.toUpperCase().charAt(0));
                }
            }
        }

        return initials.toString();
    }

    public static String capitalizeWordsWithUnderscore(String input) {

        if (input == null || input.isBlank()) {
            return input;
        }

        String[] words = input.toLowerCase()
                .replace("_", " ")
                .split("\\s+");

        return Arrays.stream(words)
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
