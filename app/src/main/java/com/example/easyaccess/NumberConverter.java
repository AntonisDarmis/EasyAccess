package com.example.easyaccess;

import java.util.HashMap;

public class NumberConverter {
        private static final HashMap<String, Integer> numberWords = new HashMap<>();

        static {
            numberWords.put("zero", 0);
            numberWords.put("one", 1);
            numberWords.put("two", 2);
            numberWords.put("three", 3);
            numberWords.put("four", 4);
            numberWords.put("five", 5);
            numberWords.put("six", 6);
            numberWords.put("seven", 7);
            numberWords.put("eight", 8);
            numberWords.put("nine", 9);
            numberWords.put("ten", 10);
            numberWords.put("eleven", 11);
            numberWords.put("twelve", 12);
            numberWords.put("thirteen", 13);
            numberWords.put("fourteen", 14);
            numberWords.put("fifteen", 15);
            numberWords.put("sixteen", 16);
            numberWords.put("seventeen", 17);
            numberWords.put("eighteen", 18);
            numberWords.put("nineteen", 19);
            numberWords.put("twenty", 20);
            numberWords.put("thirty", 30);
            numberWords.put("forty", 40);
            numberWords.put("fifty", 50);
            numberWords.put("sixty", 60);
            numberWords.put("seventy", 70);
            numberWords.put("eighty", 80);
            numberWords.put("ninety", 90);
            numberWords.put("hundred", 100);
            numberWords.put("thousand", 1000);
            numberWords.put("million", 1000000);
            numberWords.put("billion", 1000000000);
        }

        public static long convertWordsToNumber(String words) {
            String[] wordArray = words.toLowerCase().split("\\s+");
            long result = 0;
            long currentNumber = 0;

            for (String word : wordArray) {
                if (numberWords.containsKey(word)) {
                    long value = numberWords.get(word);
                    if (value >= 1000) {
                        result += currentNumber * value;
                        currentNumber = 0;
                    } else if (value >= 100) {
                        currentNumber *= value;
                    } else {
                        currentNumber += value;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid number word: " + word);
                }
            }

            return result + currentNumber;
        }
}
