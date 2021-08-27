package be.ac.umons.rocabenchmarks;

import static be.ac.umons.rocabenchmarks.JSONSymbol.toSymbol;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility functions to convert a character word (or a string) to a JSONSymbol word, and vice-versa.
 * 
 * @author Gaëtan Staquet
 */
public class WordConversion {
    public static Word<JSONSymbol> fromStringToJSONSymbolWord(String string) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        boolean inString = false;
        String lastSymbols = new String();
        for (int i = 0 ; i < string.length() ; i++) {
            char character = string.charAt(i);
            if (character == '"') {
                if (!inString) {
                    if (!lastSymbols.isEmpty()) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
                    wordBuilder.add(toSymbol('"'));
                }
                else {
                    lastSymbols = lastSymbols.concat("\"");
                }
                inString = !inString;
            }
            else if (character == ':') {
                if (inString) {
                    wordBuilder.add(toSymbol(':'));
                }
                else {
                    lastSymbols = lastSymbols.concat(":");
                }
            }
            else if (character == '{') {
                if (inString) {
                    wordBuilder.add(toSymbol('{'));
                }
                else {
                    lastSymbols = lastSymbols.concat("{");
                }
            }
            else if (character == ' ') {
                if (inString) {
                    wordBuilder.add(toSymbol(' '));
                }
            }
            else {
                if (!lastSymbols.isEmpty()) {
                    wordBuilder.add(toSymbol(lastSymbols));
                    lastSymbols = new String();
                }
                wordBuilder.add(toSymbol(character));
            }
        }
        
        return wordBuilder.toWord();
    }

    public static Word<JSONSymbol> fromCharacterWordToJSONSymbolWord(Word<Character> word) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        boolean inString = false;
        String lastSymbols = new String();
        for (int i = 0 ; i < word.length() ; i++) {
            char character = word.getSymbol(i);
            if (character == '"') {
                if (!inString) {
                    if (!lastSymbols.isEmpty()) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
                    wordBuilder.add(toSymbol('"'));
                }
                else {
                    lastSymbols = lastSymbols.concat("\"");
                }
                inString = !inString;
            }
            else if (character == ':') {
                if (inString) {
                    wordBuilder.add(toSymbol(':'));
                }
                else {
                    lastSymbols = lastSymbols.concat(":");
                }
            }
            else if (character == '{') {
                if (inString) {
                    wordBuilder.add(toSymbol('{'));
                }
                else {
                    lastSymbols = lastSymbols.concat("{");
                }
            }
            else if (character == ' ') {
                if (inString) {
                    wordBuilder.add(toSymbol(' '));
                }
            }
            else {
                if (!lastSymbols.isEmpty()) {
                    wordBuilder.add(toSymbol(lastSymbols));
                    lastSymbols = new String();
                }
                wordBuilder.add(toSymbol(character));
            }
        }
        
        return wordBuilder.toWord();
    }

    public static Word<Character> fromJSONSymbolWordToCharacterWord(Word<JSONSymbol> word) {
        WordBuilder<Character> wordBuilder = new WordBuilder<>();
        for (JSONSymbol symbol : word) {
            String string = symbol.toString();
            for (int i = 0 ; i < string.length() ; i++) {
                Character c = string.charAt(i);
                wordBuilder.add(c);
            }
        }
        return wordBuilder.toWord();
    }

    public static String fromJSONSymbolWordToString(Word<JSONSymbol> word) {
        StringBuilder stringBuilder = new StringBuilder();
        for (JSONSymbol symbol : word) {
            String string = symbol.toString();
            for (int i = 0 ; i < string.length() ; i++) {
                stringBuilder.append(string.charAt(i));
            }
        }
        return stringBuilder.toString();
    }
}
