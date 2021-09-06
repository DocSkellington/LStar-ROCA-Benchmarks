package be.ac.umons.jsonroca.oracles;

import net.automatalib.words.Word;

public class Utils {
    public static String wordToString(Word<Character> word) {
        StringBuilder builder = new StringBuilder(word.size());
        for (Character character : word) {
            builder.append(character);
        }
        return builder.toString();
    }

    public static int countUnmatched(String word) {
        int numberUnmatchedOpen = 0;
        boolean inString = false;
        boolean previousWasEscape = false;
        for (int i = 0 ; i < word.length() ; i++) {
            char character = word.charAt(i);
            if (!previousWasEscape && (character == '"' || character == '\'')) {
                inString = !inString;
            }

            if (!inString && (character == '{' || character == '[')) {
                numberUnmatchedOpen++;
            }
            else if (!inString && (character == '}' || character == ']')) {
                numberUnmatchedOpen--;
            }
            previousWasEscape = (character == '\\');
        }

        return numberUnmatchedOpen;
    }

    public static boolean validWord(String word) {
        if (word.isEmpty() || word.charAt(0) != '{' || word.charAt(word.length() - 1) != '}') {
            return false;
        }

        int numberUnmatchedOpen = 0;
        boolean firstObject = true;
        boolean inString = false;
        boolean previousWasEscape = false;
        for (int i = 0 ; i < word.length() ; i++) {
            char character = word.charAt(i);
            if (numberUnmatchedOpen == 0 && !firstObject) {
                return false;
            }
            if (!previousWasEscape && (character == '"' || character == '\'')) {
                inString = !inString;
            }
            if (!inString && (character == '{' || character == '[')) {
                if (numberUnmatchedOpen == 0) {
                    firstObject = false;
                }
                numberUnmatchedOpen++;
            }
            else if (!inString && (character == '}' || character == ']')) {
                if (numberUnmatchedOpen == 0) {
                    return false;
                }
                numberUnmatchedOpen--;
            }

            previousWasEscape = (character == '\\');
        }

        return true;
    }
}
