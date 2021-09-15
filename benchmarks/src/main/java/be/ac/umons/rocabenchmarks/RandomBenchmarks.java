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

import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ObservationTableWithCounterValuesROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.SingleQueryOracle;
import de.learnlib.filter.cache.roca.CounterValueHashCacheOracle;
import de.learnlib.filter.cache.roca.ROCAHashCacheOracle;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.oracle.CounterValueCounterOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterEQOracle;
import de.learnlib.filter.statistic.oracle.ROCACounterOracle;
import de.learnlib.oracle.equivalence.roca.ROCASimulatorEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonCounterEQOracle;
import de.learnlib.oracle.equivalence.roca.RestrictedAutomatonROCASimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.ROCASimulatorOracle;
import de.learnlib.oracle.membership.roca.CounterValueOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class RandomBenchmarks {
    private final CSVPrinter csvPrinter;
    private final int nColumns;
    private final Duration timeout;

    public RandomBenchmarks(final Path pathToCSVFile, final Duration timeout) throws IOException {
        csvPrinter = new CSVPrinter(new FileWriter(pathToCSVFile.toFile()), CSVFormat.DEFAULT);
        // @formatter:off
        List<String> header = Arrays.asList(
            "Target ROCA size",
            "Alphabet size",
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
            "Result target size",
            "DOT"
        );
        // @formatter:on
        this.nColumns = header.size();
        csvPrinter.printRecord(header);
        this.timeout = timeout;
        csvPrinter.flush();
    }

    public void runBenchmarks(final Random rand, final int minSize, final int maxSize, final int minAlphabetSize,
            final int maxAlphabetSize, final int nRepetitions) throws InterruptedException, IOException {
        for (int size = minSize; size <= maxSize; size++) {
            for (int alphabetSize = minAlphabetSize; alphabetSize <= maxAlphabetSize; alphabetSize++) {
                for (int i = 0; i < nRepetitions; i++) {
                    System.out.println((i + 1) + "/" + nRepetitions);
                    Alphabet<Integer> alphabet = Alphabets.integers(0, alphabetSize - 1);
                    RandomROCA<Integer> randomROCA = new RandomROCA<>(alphabet, size, 0.5);
                    runExperiment(randomROCA, timeout);
                    System.gc();
                }
            }
        }
    }

    private <I> void runExperiment(RandomROCA<I> randomROCA, final Duration timeout)
            throws InterruptedException, IOException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        SimpleProfiler.reset();
        ROCA<?, I> target = randomROCA.getROCA();
        Alphabet<I> alphabet = randomROCA.getAlphabet();

        SingleQueryOracle.SingleQueryOracleROCA<I> sul = new ROCASimulatorOracle<>(target);
        ROCAHashCacheOracle<I> sulCache = new ROCAHashCacheOracle<>(sul);
        ROCACounterOracle<I> membershipOracle = new ROCACounterOracle<>(sulCache, "membership queries");

        SingleQueryOracle.SingleQueryCounterValueOracle<I> counterValue = new CounterValueOracle<>(target);
        CounterValueHashCacheOracle<I> counterValueCache = new CounterValueHashCacheOracle<>(counterValue);
        CounterValueCounterOracle<I> counterValueOracle = new CounterValueCounterOracle<>(counterValueCache,
                "counter value queries");

        EquivalenceOracle.ROCAEquivalenceOracle<I> eqOracle = new ROCASimulatorEQOracle<>(target);
        ROCACounterEQOracle<I> equivalenceOracle = new ROCACounterEQOracle<>(eqOracle, "equivalence queries");

        RestrictedAutomatonROCASimulatorEQOracle<I> partialEqOracle = new RestrictedAutomatonROCASimulatorEQOracle<>(
                target, alphabet);
        RestrictedAutomatonCounterEQOracle<I> partialEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                partialEqOracle, "partial equivalence queries");

        LStarROCA<I> lstar_roca = new LStarROCA<>(membershipOracle, counterValueOracle, partialEquivalenceOracle,
                alphabet);

        ROCAExperiment<I> experiment = new ROCAExperiment<>(lstar_roca, equivalenceOracle, alphabet);
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
        Exception error = null;
        Stopwatch watch = Stopwatch.createStarted();
        try {
            handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            finished = true;
        } catch (TimeoutException e) {
            handler.cancel(true);
            finished = false;
        } catch (ExecutionException e) {
            handler.cancel(true);
            error = e;
            finished = false;
            e.printStackTrace(System.err);
        }
        watch.stop();
        executor.shutdownNow();

        List<Object> results = new LinkedList<>();
        results.add(target.size());
        results.add(alphabet.size());
        if (finished) {
            ROCA<?, I> learntROCA = experiment.getFinalHypothesis();
            ObservationTableWithCounterValuesROCA<I> table = lstar_roca.getObservationTable();

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
            results.add(learntROCA.size());

            StringBuilder sb = new StringBuilder();
            GraphDOT.write(learntROCA, sb);
            results.add(sb.toString());
        } else if (error != null) {
            for (int i = results.size() - 1; i < nColumns; i++) {
                results.add("Error");
            }
            results.add(error.toString());
        } else {
            for (int i = results.size() - 1; i < nColumns; i++) {
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
