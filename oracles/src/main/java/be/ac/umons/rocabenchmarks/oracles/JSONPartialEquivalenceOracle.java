package be.ac.umons.rocabenchmarks.oracles;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;

public class JSONPartialEquivalenceOracle<I> implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<I> {

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> arg0, Collection<? extends I> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getCounterLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCounterLimit(int arg0) {
        // TODO Auto-generated method stub
        
    }
    
}
