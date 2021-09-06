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

        base = "\"pro\":{\"i\":1}";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        System.out.println(result);
        target = Word.fromSymbols(toSymbol('"'), toSymbol("pro"), toSymbol("\""), toSymbol(":{"), toSymbol('"'), toSymbol('i'), toSymbol('"'), toSymbol(":"), toSymbol('1'), toSymbol('}'));
        Assert.assertEquals(result, target);
        
        // We ignore some spaces
        base = " \"pro\" : { \"i\" : 1 } ";
        result = WordConversion.fromStringToJSONSymbolWord(base);
        System.out.println(result);
        target = Word.fromSymbols(toSymbol('"'), toSymbol("pro"), toSymbol('"'), toSymbol(":{"), toSymbol('"'), toSymbol('i'), toSymbol('"'), toSymbol(":"), toSymbol('1'), toSymbol('}'));
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
