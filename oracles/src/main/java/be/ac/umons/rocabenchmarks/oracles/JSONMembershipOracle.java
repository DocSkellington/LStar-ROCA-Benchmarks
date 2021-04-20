package be.ac.umons.rocabenchmarks.oracles;

import java.util.Collection;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;

public class JSONMembershipOracle<I> implements MembershipOracle.RestrictedAutomatonMembershipOracle<I> {

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> arg0) {
        // TODO Auto-generated method stub
        
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
