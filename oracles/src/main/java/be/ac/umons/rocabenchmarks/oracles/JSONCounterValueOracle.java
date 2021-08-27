package be.ac.umons.rocabenchmarks.oracles;

import be.ac.umons.rocabenchmarks.JSONSymbol;
import be.ac.umons.rocabenchmarks.WordConversion;
import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryCounterValueOracle;
import net.automatalib.words.Word;

public class JSONCounterValueOracle implements SingleQueryCounterValueOracle<JSONSymbol> {

    @Override
    public Integer answerQuery(Word<JSONSymbol> prefix, Word<JSONSymbol> suffix) {
        Word<JSONSymbol> word = prefix.concat(suffix);
        return Utils.countUnmatched(WordConversion.fromJSONSymbolWordToString(word));
    }
    
}
