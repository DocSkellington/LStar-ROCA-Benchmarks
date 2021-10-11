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
package be.ac.umons.rocabenchmarks;

import java.time.Duration;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.roca.ROCARandomEQOracle;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.automata.oca.automatoncountervalues.ROCAFromDescription;
import net.automatalib.words.Alphabet;

public class ROCAEquivalenceOracle<I> extends ROCARandomEQOracle<I> {

    private final Duration timeLimit;

    public ROCAEquivalenceOracle(Duration timeLimit, ROCA<?, I> reference) {
        this(timeLimit, reference, reference.getAlphabet());
    }

    public ROCAEquivalenceOracle(Duration timeLimit, ROCA<?, I> reference, Alphabet<I> alphabet) {
        this(timeLimit, reference, alphabet, new Random());
    }

    public ROCAEquivalenceOracle(Duration timeLimit, ROCA<?, I> reference, Alphabet<I> alphabet, Random rand) {
        super(reference, alphabet, rand);
        this.timeLimit = timeLimit;
    }

    private @Nullable DefaultQuery<I, Boolean> findCounterExampleRun(ROCAFromDescription<?, I> hypothesis, Collection<? extends I> inputs) {
        return super.findCounterExample(hypothesis, inputs);
    }
    
    @Override
    public @Nullable DefaultQuery<I, Boolean> findCounterExample(ROCAFromDescription<?, I> hypothesis,
            Collection<? extends I> inputs) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<DefaultQuery<I, Boolean>> handler = executor.submit(new Callable<DefaultQuery<I, Boolean>>() {
            @Override
            public DefaultQuery<I, Boolean> call() throws Exception {
                return findCounterExampleRun(hypothesis, inputs);
            }
        });

        DefaultQuery<I, Boolean> query;
        try {
            query = handler.get(timeLimit.toMillis(), TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException | InterruptedException | ExecutionException e) {
            handler.cancel(true);
            query = null;
        }
        executor.shutdown();

        return query;
    }
}