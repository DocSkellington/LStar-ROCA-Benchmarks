package be.ac.umons.jsonroca;

import static be.ac.umons.jsonroca.JSONSymbol.toSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility functions to convert a character word (or a string) to a JSONSymbol
 * word, and vice-versa.
 * 
 * @author Gaëtan Staquet
 */
public class WordConversion {
    public static Word<JSONSymbol> fromJSONDocumentToJSONSymbolWord(JSONObject document, boolean shuffleKeys, Random rand) {
        WordBuilder<JSONSymbol> wordBuilder = new WordBuilder<>();
        wordBuilder.add(toSymbol("{"));
        fromJSONObjectToJSONWord(document, shuffleKeys, rand, wordBuilder);
        wordBuilder.add(toSymbol("}"));
        return wordBuilder.toWord();
    }

    public static Word<JSONSymbol> fromJSONDocumentToJSONSymbolWord(JSONObject document, boolean shuffleKeys) {
        return fromJSONDocumentToJSONSymbolWord(document, shuffleKeys, new Random());
    }

    public static Word<JSONSymbol> fromJSONDocumentToJSONSymbolWord(JSONObject document) {
        return fromJSONDocumentToJSONSymbolWord(document, true);
    }

    private static void fromJSONObjectToJSONWord(JSONObject object, boolean shuffleKeys, Random rand, WordBuilder<JSONSymbol> wordBuilder) {
        List<String> keys = new ArrayList<>(object.keySet());
        if (shuffleKeys) {
            Collections.shuffle(keys, rand);
        }
        boolean first =  true;
        for (String key : keys) {
            if (!first) {
                wordBuilder.add(toSymbol(","));
            }
            first = false;
            wordBuilder.add(toSymbol("\"" + key + "\""));

            Object o = object.get(key);
            if (o instanceof JSONObject) {
                wordBuilder.add(toSymbol(":{"));
                fromJSONObjectToJSONWord((JSONObject) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("}"));
            }
            else if (o instanceof JSONArray) {
                wordBuilder.add(toSymbol(":["));
                fromJSONArrayToJSONWord((JSONArray) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("]"));
            }
            else if (o instanceof Boolean) {
                wordBuilder.add(toSymbol(":"));
                wordBuilder.add(toSymbol(o.toString()));
            }
            else {
                wordBuilder.add(toSymbol(":"));
                wordBuilder.add(toSymbol("\"" + o.toString() + "\""));
            }
        }
    }

    private static void fromJSONArrayToJSONWord(JSONArray array, boolean shuffleKeys, Random rand, WordBuilder<JSONSymbol> wordBuilder) {
        boolean first = true;
        for (Object o : array) {
            if (!first) {
                wordBuilder.add(toSymbol(","));
            }
            first = false;
            if (o instanceof JSONObject) {
                wordBuilder.add(toSymbol("{"));
                fromJSONObjectToJSONWord((JSONObject) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("}"));
            }
            else if (o instanceof JSONArray) {
                wordBuilder.add(toSymbol("["));
                fromJSONArrayToJSONWord((JSONArray) o, shuffleKeys, rand, wordBuilder);
                wordBuilder.add(toSymbol("]"));
            }
            else if (o instanceof Boolean) {
                wordBuilder.add(toSymbol(o.toString()));
            }
            else {
                wordBuilder.add(toSymbol("\"" + o.toString() + "\""));
            }
        }
    }

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
                        wordBuilder.add(toSymbol("\"" + lastSymbols + "\""));
                        lastSymbols = new String();
                    }
                    inString = false;
                } else {
                    escaped = false;
                    lastSymbols = lastSymbols + character;
                }
            } else {
                if (character == '"' || Character.isDigit(character) || character == ']' || character == '}' || character == ',') {
                    if (!lastSymbols.isEmpty()) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
                    if (character != '"') {
                        wordBuilder.add(toSymbol(character));
                    }
                    if (character == '"') {
                        inString = true;
                    }
                }
                else if (character != ' ') {
                    if (Character.isAlphabetic(character) && (lastSymbols.equals(":") || lastSymbols.equals(","))) {
                        wordBuilder.add(toSymbol(lastSymbols));
                        lastSymbols = new String();
                    }
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
