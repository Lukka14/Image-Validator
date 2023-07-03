package com.example.springboottest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPatternExample {
    public static void main(String[] args) {
        String input = "This is a \"sample\" string \"with\" values in quotes";
        List<String> values = getValuesInQuotes(input);

        for (String value : values) {
            System.out.println(value);
        }
    }

    public static List<String> getValuesInQuotes(String input) {
        List<String> values = new ArrayList<>();

        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String value = matcher.group(1);
            values.add(value);
        }

        return values;
    }
}