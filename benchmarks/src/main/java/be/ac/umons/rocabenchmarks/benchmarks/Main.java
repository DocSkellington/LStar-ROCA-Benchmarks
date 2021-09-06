package be.ac.umons.rocabenchmarks.benchmarks;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.SingleQueryOracle;
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
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class Main {
    private static int NUMBER_OF_TESTS = 1000;

    public static void main(String[] args) throws IOException {
        Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        Random rand = new Random(302);
        for (int size = 1 ; size <= 1 ; size++) {
            for (int i = 0 ; i < 100 ; i++) {
                RandomROCA<Character> randomROCA = new RandomROCA<>(rand, alphabet, size, 0.5);
                runRandomROCA(randomROCA);
            }
        }

        // SchemaStore schemaStore = new SchemaStore();
        // Schema schema;
        // try {
        //     URL url = Main.class.getResource("/schema/codecov.json");
        //     schema = schemaStore.loadSchema(url.toURI(), false);
        // } catch (GenerationException e) {
        //     e.printStackTrace();
        //     return;
        // } catch (URISyntaxException e) {
        //     e.printStackTrace();
        //     return;
        // }

        // Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "JSON");
        // File outputDirectory = outputPath.toFile();
        // if (outputDirectory.exists()) {
        //     deleteDirectory(outputDirectory);
        // }
        // assert outputDirectory.mkdir();
        // outputDirectory = outputPath.toFile();
        // outputDirectory.mkdir();

        // GrowingAlphabet<JSONSymbol> alphabet = new GrowingMapAlphabet<>();

        // Random random = new Random();

        // MembershipOracle.ROCAMembershipOracle<JSONSymbol> membershipOracle = new JSONMembershipOracle(schema);
        // MembershipOracle.CounterValueOracle<JSONSymbol> counterValueOracle = new JSONCounterValueOracle();
        // EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> partialEquivalenceOracle = new JSONPartialEquivalenceOracle(NUMBER_OF_TESTS, schema, schemaStore, random);
        // EquivalenceOracle.ROCAEquivalenceOracle<JSONSymbol> equivalenceOracle = new JSONEquivalenceOracle(NUMBER_OF_TESTS, schema, schemaStore, random);

        // LStarROCAGrowingAlphabet<JSONSymbol> learningAlgorithm = new LStarROCAGrowingAlphabet<>(membershipOracle, counterValueOracle, partialEquivalenceOracle, alphabet);
        // ROCAExperiment<JSONSymbol> experiment = new ROCAExperiment<>(learningAlgorithm, equivalenceOracle, alphabet, outputPath);
        // experiment.setLogModels(false);
        // experiment.setProfile(true);
        // experiment.run();

        // ROCA<?, JSONSymbol> result = experiment.getFinalHypothesis();

        // System.out.println("-------------------------------------------------------");

        // // profiling
        // System.out.println(SimpleProfiler.getResults());

        // // learning statistics
        // System.out.println(experiment.getRounds().getSummary());
        // // System.out.println(equivalenceOracle.getStatisticalData().getSummary());
        // // System.out.println(restrictedEquivalenceOracle.getStatisticalData().getSummary());
        // // System.out.println(membershipOracle.getStatisticalData().getSummary());
        // // System.out.println(counterValueOracle.getStatisticalData().getSummary());

        // // model statistics
        // System.out.println("States: " + result.size());
        // System.out.println("Sigma: " + alphabet.size());

        // // show model
        // System.out.println();
        // System.out.println("Model: ");
        // GraphDOT.write(result, System.out); // may throw IOException!

        // // Visualization.visualize(result);

        // System.out.println("-------------------------------------------------------");

        // // OTUtils.displayHTMLInBrowser(lstar_roca.getObservationTable().toClassicObservationTable());

        // Path finalTable = outputPath.resolve("final.html");
        // OTUtils.writeHTMLToFile(learningAlgorithm.getObservationTable().toClassicObservationTable(), finalTable.toFile());
        // FileWriter finalModel = new FileWriter(outputPath.resolve("final.dot").toFile());
        // GraphDOT.write(result, finalModel);
        // finalModel.close();
    }

    private static boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File f : allContents) {
                deleteDirectory(f);
            }
        }
        return directory.delete();
    }

    private static <I> ROCA<?, I> runRandomROCA(RandomROCA<I> randomROCA) {
        ROCA<?, I> target = randomROCA.getROCA();
        Alphabet<I> alphabet = randomROCA.getAlphabet();

        SingleQueryOracle.SingleQueryOracleROCA<I> sul = new ROCASimulatorOracle<>(
                target);
        ROCACounterOracle<I> membershipOracle = new ROCACounterOracle<>(sul,
                "membership queries");

        SingleQueryOracle.SingleQueryCounterValueOracle<I> counterValue = new CounterValueOracle<>(target);
        // CounterValueHashCacheOracle<I> counterValueCache = new CounterValueHashCacheOracle<>(counterValue);
        CounterValueCounterOracle<I> counterValueOracle = new CounterValueCounterOracle<>(counterValue,
                "counter value queries");

        EquivalenceOracle.ROCAEquivalenceOracle<I> eqOracle = new ROCASimulatorEQOracle<>(target);
        ROCACounterEQOracle<I> equivalenceOracle = new ROCACounterEQOracle<>(eqOracle, "equivalence queries");

        RestrictedAutomatonROCASimulatorEQOracle<I> resEqOracle = new RestrictedAutomatonROCASimulatorEQOracle<>(target,
                alphabet);
        RestrictedAutomatonCounterEQOracle<I> restrictedEquivalenceOracle = new RestrictedAutomatonCounterEQOracle<>(
                resEqOracle, "partial equivalence queries");

        LStarROCA<I> lstar_roca = new LStarROCA<>(membershipOracle, counterValueOracle, restrictedEquivalenceOracle,
                alphabet);

        ROCAExperiment<I> experiment = new ROCAExperiment<>(lstar_roca, equivalenceOracle, alphabet);
        experiment.setLogModels(false);
        experiment.setProfile(true);

        experiment.run();

        ROCA<?, I> result = experiment.getFinalHypothesis();

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(equivalenceOracle.getStatisticalData().getSummary());
        System.out.println(restrictedEquivalenceOracle.getStatisticalData().getSummary());
        System.out.println(membershipOracle.getStatisticalData().getSummary());
        System.out.println(counterValueOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + alphabet.size());

        return result;
    }
}
