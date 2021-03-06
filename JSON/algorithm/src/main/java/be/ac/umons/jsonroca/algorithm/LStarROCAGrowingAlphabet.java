/* Copyright (C) 2021 – University of Mons, University Antwerpen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ac.umons.jsonroca.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.util.MQUtil;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * A version of L* for ROCAs that extends alphabet with new symbols from the
 * counterexamples.
 * 
 * @author Gaëtan Staquet
 */
public final class LStarROCAGrowingAlphabet<I> extends LStarROCA<I> {
    private static final LearnLogger LOGGER = LearnLogger.getLogger(LStarROCAGrowingAlphabet.class);

    public LStarROCAGrowingAlphabet(MembershipOracle.ROCAMembershipOracle<I> membershipOracle,
            MembershipOracle.CounterValueOracle<I> counterValueOracle,
            EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<I> automatonWithCounterValuesEquivalenceOracle,
            GrowingAlphabet<I> alphabet) {
        super(membershipOracle, counterValueOracle, automatonWithCounterValuesEquivalenceOracle, alphabet);
    }

    private void addNewSymbolsFromWord(Word<I> word) {
        for (I symbol : word) {
            addAlphabetSymbol(symbol);
        }
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        lengthLongestCounterexample = Math.max(ceQuery.getInput().length(), lengthLongestCounterexample);
        // 1. We compute the new counter limit
        List<DefaultQuery<I, Integer>> counterValueQueries = new ArrayList<>(ceQuery.getInput().length());
        for (Word<I> prefix : ceQuery.getInput().prefixes(false)) {
            DefaultQuery<I, Integer> query = new DefaultQuery<>(prefix);
            counterValueQueries.add(query);
        }
        counterValueOracle.processQueries(counterValueQueries);
        // @formatter:off
        counterLimit = counterValueQueries.stream()
            .map(query -> query.getOutput())
            .max(Integer::compare)
            .get();
        // @formatter:on
        LOGGER.logEvent("Counter limit: " + counterLimit);

        // 2. We increase the counter limit in the oracles
        restrictedAutomatonEquivalenceOracle.setCounterLimit(counterLimit);

        // 3. If it's the first time we refine the hypothesis, we initialize the table
        // using the counter-example.
        // If it is not the firs time, we refine the table
        List<List<Row<I>>> unclosed;
        if (!table.isInitialized()) {
            Word<I> counterexample = ceQuery.getInput();
            GrowingAlphabet<I> growingAlphabet = Alphabets.toGrowingAlphabetOrThrowException(alphabet);
            for (I symbol : counterexample) {
                growingAlphabet.addSymbol(symbol);
            }

            final List<Word<I>> initialPrefixes = counterexample.prefixes(false);
            final List<Word<I>> initialSuffixes = initialSuffixes();
            for (Word<I> suffix : initialSuffixes) {
                for (I symbol : suffix) {
                    growingAlphabet.addSymbol(symbol);
                }
            }

            table.setInitialCounterLimit(counterLimit);
            unclosed = table.initialize(initialPrefixes, initialSuffixes, membershipOracle);
            completeConsistentTable(unclosed, table.isInitialConsistencyCheckRequired());
        } else if (!MQUtil.isCounterexample(ceQuery, hypothesis)) {
            return false;
        } else {
            Word<I> counterexample = ceQuery.getInput();
            addNewSymbolsFromWord(counterexample);
            unclosed = table.increaseCounterLimit(counterLimit, counterexample.prefixes(false), Collections.emptyList(),
                    membershipOracle);
            LOGGER.logEvent("Counter limit increased in table");
            completeConsistentTable(unclosed, true);
        }

        // 4. We learn the DFA up to the counter value
        LOGGER.logEvent("Starting to learn the DFA up to the counter limit");
        learnDFA();

        return true;
    }

    @Override
    protected void learnDFA() {
        while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return;
            }
            updateHypothesis();

            SimpleProfiler.start(COUNTEREXAMPLE_DFA_PROFILE_KEY);
            DefaultQuery<I, Boolean> ce = restrictedAutomatonEquivalenceOracle.findCounterExample(hypothesis, alphabet);
            SimpleProfiler.stop(COUNTEREXAMPLE_DFA_PROFILE_KEY);

            if (ce == null) {
                return;
            }
            assert MQUtil.isCounterexample(ce, hypothesis);
            LOGGER.logCounterexample(ce.getInput().toString());
            addNewSymbolsFromWord(ce.getInput());
            lengthLongestCounterexample = Math.max(ce.getInput().length(), lengthLongestCounterexample);

            int oldDistinctRows = table.numberOfDistinctRows();
            int oldSuffixes = table.numberOfSuffixes();

            List<List<Row<I>>> unclosed = table.addShortPrefixes(ce.getInput().prefixes(false), membershipOracle);
            completeConsistentTable(unclosed, true);

            assert table.numberOfDistinctRows() > oldDistinctRows || table.numberOfSuffixes() > oldSuffixes
                    : "Nothing was learnt during the last iteration for DFA";
        }
    }
}
