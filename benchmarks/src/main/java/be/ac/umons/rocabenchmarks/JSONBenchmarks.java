package be.ac.umons.rocabenchmarks;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Stopwatch;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.jsonroca.algorithm.LStarROCAGrowingAlphabet;
import be.ac.umons.jsonroca.oracles.JSONCounterValueOracle;
import be.ac.umons.jsonroca.oracles.JSONEquivalenceOracle;
import be.ac.umons.jsonroca.oracles.JSONMembershipOracle;
import be.ac.umons.jsonroca.oracles.JSONPartialEquivalenceOracle;
import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ObservationTableWithCounterValuesROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.roca.CounterValueHashCacheOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.CounterValueCounterOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterEQOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;

public class JSONBenchmarks {
    private final CSVPrinter csvPrinter;
    private final int nColumns;
    private final Duration timeout;

    public JSONBenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        // @formatter:off
        List<String> header = Arrays.asList(
            "Total time (ms)",
            "ROCA counterexample time (ms)",
            "DFA counterexample time (ms)",
            "Learning ROCA time (ms)",
            "Table time (ms)",
            "Membership queries",
            "Counter value queries",
            "Partial equivalence queries",
            "Equivalence queries",
            "Rounds",
            "|R|",
            "|S|",
            "|Ŝ \\ S|",
            "# of bin rows",
            "Result alphabet size",
            "Result target size"
        );
        // @formatter:on
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        this.timeout = timeout;
        csvPrinter.flush();
    }

    public void runBenchmarks(final Random rand, final Schema schema, final SchemaStore schemaStore, final int nTests, final int nRepetitions) throws GenerationException, InterruptedException, IOException {
        for (int i = 0 ; i < nRepetitions ; i++) {
            System.out.println((i+1) + "/" + nRepetitions);
            runExperiment(rand, schema, schemaStore, nTests);
        }
    }

    private void runExperiment(final Random rand, final Schema schema, final SchemaStore schemaStore, final int nTests)
            throws GenerationException, InterruptedException, IOException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        SimpleProfiler.reset();
        GrowingAlphabet<JSONSymbol> alphabet = new GrowingMapAlphabet<>();

        MembershipOracle.ROCAMembershipOracle<JSONSymbol> sul = new JSONMembershipOracle(schema);
        ROCAHashCacheOracle<JSONSymbol> sulCache = new ROCAHashCacheOracle<>(sul);
        ROCACounterOracle<JSONSymbol> membershipOracle = new ROCACounterOracle<>(sulCache, "membership queries");

        MembershipOracle.CounterValueOracle<JSONSymbol> counterValue = new JSONCounterValueOracle();
        CounterValueHashCacheOracle<JSONSymbol> counterValueCache = new CounterValueHashCacheOracle<>(counterValue);
        CounterValueCounterOracle<JSONSymbol> counterValueOracle = new CounterValueCounterOracle<>(counterValueCache,
                "counter value queries");

        EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEqOracle = new JSONPartialEquivalenceOracle(
                nTests, schema, schemaStore, rand);
        RestrictedAutomatonCounterEQOracle<JSONSymbol> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> eqOracle = new JSONEquivalenceOracle(nTests, schema,
                schemaStore, rand);
        ROCACounterEQOracle<JSONSymbol> equivalenceOracle = new ROCACounterEQOracle<>(eqOracle, "equivalence queries");

        LStarROCAGrowingAlphabet<JSONSymbol> lstar_roca = new LStarROCAGrowingAlphabet<>(membershipOracle,
                counterValueOracle, partialEquivalenceOracle, alphabet);
        ROCAExperiment<JSONSymbol> experiment = new ROCAExperiment<>(lstar_roca, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        final Future<Void> handler = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                experiment.run();
                return null;
            }
        });

        boolean finished;
        boolean error = false;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            finished = true;
        } catch (TimeoutException e) {
            handler.cancel(true);
            finished = false;
        } catch (ExecutionException e) {
            handler.cancel(true);
            error = true;
            finished = false;
        }
        watch.stop();
        executor.shutdownNow();

        List<Object> results = new LinkedList<>();
        if (finished) {
            ROCA<?, JSONSymbol> learntROCA = experiment.getFinalHypothesis();
            ObservationTableWithCounterValuesROCA<JSONSymbol> table = lstar_roca.getObservationTable();

            results.add(watch.elapsed().toMillis());
            results.add(getProfilerTime(ROCAExperiment.COUNTEREXAMPLE_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.COUNTEREXAMPLE_DFA_PROFILE_KEY));
            results.add(getProfilerTime(ROCAExperiment.LEARNING_ROCA_PROFILE_KEY));
            results.add(getProfilerTime(LStarROCA.CLOSED_TABLE_PROFILE_KEY));
            results.add(membershipOracle.getStatisticalData().getCount());
            results.add(counterValueOracle.getStatisticalData().getCount());
            results.add(partialEquivalenceOracle.getStatisticalData().getCount());
            results.add(equivalenceOracle.getStatisticalData().getCount());
            results.add(experiment.getRounds().getCount());
            results.add(table.numberOfShortPrefixRows());
            results.add(table.numberOfClassicalSuffixes());
            results.add(table.numberOfForLanguageOnlySuffixes());
            results.add(table.numberOfBinShortPrefixRows());
            results.add(alphabet.size());
            results.add(learntROCA.size());
        } else if (error) {
            for (int i = 0; i < nColumns; i++) {
                results.add("Error");
            }
        } else {
            for (int i = 0; i < nColumns; i++) {
                results.add("Timeout");
            }
        }

        csvPrinter.printRecord(results);
        csvPrinter.flush();
    }

    private long getProfilerTime(String key) {
        Counter counter = SimpleProfiler.cumulated(key);
        if (counter == null) {
            return 0;
        } else {
            return counter.getCount();
        }
    }
}
