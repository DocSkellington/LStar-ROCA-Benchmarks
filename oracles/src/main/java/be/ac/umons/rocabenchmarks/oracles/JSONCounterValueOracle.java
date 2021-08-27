package be.ac.umons.rocabenchmarks.oracles;

import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryCounterValueOracle;
import net.automatalib.words.Word;

public class JSONCounterValueOracle implements SingleQueryCounterValueOracle<Character> {

    @Override
    public Integer answerQuery(Word<Character> prefix, Word<Character> suffix) {
        Word<Character> word = prefix.concat(suffix);

        return Utils.countUnmatched(word);
    }
    
}
