package com.example.easyaccess;

public class DialogFormatter {
     public static  String insertNewLineAtWordsCount(String description,int count) {
        // Split the description into words
        String[] words = description.split("\\b");

        // Initialize a counter to keep track of the number of words processed
        int wordCount = 0;

        // Initialize a StringBuilder to construct the new string
        StringBuilder resultBuilder = new StringBuilder();

        // Iterate through the words
        for (String word : words) {
            // Skip empty strings
            if (word.trim().isEmpty()) {
                continue;
            }

            // Append the word to the result
            resultBuilder.append(word).append(" ");

            // Increment the word count
            wordCount++;

            // Insert a new line after every 5 words
            if (wordCount % count == 0) {
                resultBuilder.append("\n");
            }
        }

        // Convert the StringBuilder to a string and return
        return resultBuilder.toString().trim();
    }
}
