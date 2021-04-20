package be.ac.umons.rocabenchmarks.oracles;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.oca.ROCA;

public class JSONEquivalenceOracle<I> implements EquivalenceOracle.ROCAEquivalenceOracle<I> {

    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(ROCA<?, I> arg0, Collection<? extends I> arg1) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
