package be.ac.umons.jsonroca;

import static be.ac.umons.jsonroca.JSONSymbol.toSymbol;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.automatalib.words.Word;

public class WordConversionTests {
    @Test
    public void fromStringToSymbols() {
        String base = "hello";
        Word<JSONSymbol> result = WordConversion.fromStringToJSONSymbolWord(base);
        Word<JSONSymbol> target = Word.fromSymbols(toSymbol("hello"));
        Assert.assertEquals(result, target);

        base = "\"pro\":{\"i\":1},\"other\":\"\\\\D\"";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"pro\"", ":{", "\"i\"", ":", "1", "}", ",", "\"other\"", ":", "\"\\D\"");
        Assert.assertEquals(result, target);
        
        // We ignore some spaces
        base = " \"pro\" : { \"i\" : 1 } , \"other\" : \"\\\\D\" ";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"pro\"", ":{", "\"i\"", ":", "1", "}", ",", "\"other\"", ":", "\"\\D\"");
        Assert.assertEquals(result, target);

        // Arrays and booleans
        base = "\"arrays\": [1, 2, 3], \"boolean\": false, \"other\": true";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"arrays\"", ":[", "1", ",", "2", ",", "3", "]", ",", "\"boolean\"", ":", "false", ",", "\"other\"", ":", "true");
        Assert.assertEquals(result, target);

        // Empty array
        base = "\"array\": [], \"other\": false";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"array\"", ":[", "]", ",", "\"other\"", ":", "false");
        Assert.assertEquals(result, target);

        // Empty object
        base = "\"obj\": {}";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"obj\"", ":{", "}");
        Assert.assertEquals(result, target);

        // Closing two objects in a row
        base = "\"obj\": {\"obj\": {}}";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        target = JSONSymbol.toWord("\"obj\"", ":{", "\"obj\"", ":{", "}", "}");
        Assert.assertEquals(result, target);
    }

    @Test
    public void fromSymbolToCharacter() {
        Word<JSONSymbol> base = Word.fromSymbols(toSymbol("h"), toSymbol('e'), toSymbol('l'), toSymbol('l'), toSymbol('o'));
        String result = WordConversion.fromJSONSymbolWordToString(base);
        String target = "hello";
        Assert.assertEquals(result, target);

        base = Word.fromSymbols(toSymbol('"'), toSymbol('p'), toSymbol('r'), toSymbol('o'), toSymbol("\":{"), toSymbol('"'), toSymbol('i'), toSymbol("\":"), toSymbol('1'), toSymbol('}'));
        result = WordConversion.fromJSONSymbolWordToString(base);
        target = "\"pro\":{\"i\":1}";
        Assert.assertEquals(result, target);
    }
}
