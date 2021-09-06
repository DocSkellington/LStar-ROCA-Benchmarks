package be.ac.umons.jsonroca;

import static be.ac.umons.jsonroca.JSONSymbol.toSymbol;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility functions to convert a character word (or a string) to a JSONSymbol
 * word, and vice-versa.
 * 
 * @author Gaëtan Staquet
 */
public class WordConversion {
    public static Word<JSONSymbol> fromStringToJSONSymbolWord(String string) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        boolean inString = false;
        String lastSymbols = new String();
        boolean escaped = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (inString) {
                if (character == '\\' && !escaped) {
                    escaped = true;
                } else if (character == '"' && !escaped) {
                    if (!lastSymbols.isEmpty()) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
                    wordBuilder.add(toSymbol('"'));
                    inString = false;
                } else {
                    escaped = false;
                    lastSymbols = lastSymbols + character;
                }
            } else {
                if (character == '"' || Character.isDigit(character)) {
                    if (!lastSymbols.isEmpty()) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
                    wordBuilder.add(toSymbol(character));
                    if (character == '"') {
                        inString = true;
                    }
                }
                else if (character != ' ') {
                    lastSymbols += character;
                }
            }

        }

        if (!lastSymbols.isEmpty()) {
            wordBuilder.add(toSymbol(lastSymbols));
        }
        return wordBuilder.toWord();
    }

    public static String fromJSONSymbolWordToString(Word<JSONSymbol> word) {
        StringBuilder stringBuilder = new StringBuilder();
        for (JSONSymbol symbol : word) {
            String string = symbol.toString();
            for (int i = 0; i < string.length(); i++) {
                stringBuilder.append(string.charAt(i));
            }
        }
        return stringBuilder.toString();
    }
}
