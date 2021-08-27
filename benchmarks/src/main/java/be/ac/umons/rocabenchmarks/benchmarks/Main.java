package be.ac.umons.rocabenchmarks.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

import be.ac.umons.rocabenchmarks.oracles.JSONCounterValueOracle;
import be.ac.umons.rocabenchmarks.oracles.JSONEquivalenceOracle;
import be.ac.umons.rocabenchmarks.oracles.JSONMembershipOracle;
import be.ac.umons.rocabenchmarks.oracles.JSONPartialEquivalenceOracle;
import de.learnlib.algorithms.lstar.roca.LStarROCA;
import de.learnlib.algorithms.lstar.roca.ROCAExperiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.oca.ROCA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.GrowingMapAlphabet;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;

public class Main {
    private static int NUMBER_OF_TESTS = 1000;

    public static void main(String[] args) throws IOException, GenerationException {
        SchemaStore schemaStore = new SchemaStore();
        Schema schema;
        try {
            schema = schemaStore.loadSchema(Main.class.getResource("/schema/fast.json"));
        } catch (GenerationException e) {
            e.printStackTrace();
            return;
        }

        Path outputPath = Paths.get(System.getProperty("user.home"), "Desktop", "JSON");
        File outputDirectory = outputPath.toFile();
        if (outputDirectory.exists()) {
            deleteDirectory(outputDirectory);
        }
        assert outputDirectory.mkdir();
        outputDirectory = outputPath.toFile();
        outputDirectory.mkdir();

        GrowingAlphabet<Character> alphabet = new GrowingMapAlphabet<>();

        Random random = new Random();

        MembershipOracle.ROCAMembershipOracle<Character> membershipOracle = new JSONMembershipOracle(schema);
        MembershipOracle.CounterValueOracle<Character> counterValueOracle = new JSONCounterValueOracle();
        EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<Character> partialEquivalenceOracle = new JSONPartialEquivalenceOracle(NUMBER_OF_TESTS, schema, schemaStore, random);
        EquivalenceOracle.ROCAEquivalenceOracle<Character> equivalenceOracle = new JSONEquivalenceOracle(NUMBER_OF_TESTS, schema, schemaStore, random);

        LStarROCAGrowingAlphabet<Character> learningAlgorithm = new LStarROCAGrowingAlphabet<>(membershipOracle, counterValueOracle, partialEquivalenceOracle, alphabet);
        ROCAExperiment<Character> experiment = new ROCAExperiment<>(learningAlgorithm, equivalenceOracle, alphabet, outputPath);
        experiment.setLogModels(false);
        experiment.setProfile(true);
        experiment.run();

        ROCA<?, Character> result = experiment.getFinalHypothesis();

        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        // System.out.println(equivalenceOracle.getStatisticalData().getSummary());
        // System.out.println(restrictedEquivalenceOracle.getStatisticalData().getSummary());
        // System.out.println(membershipOracle.getStatisticalData().getSummary());
        // System.out.println(counterValueOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + alphabet.size());

        // show model
        System.out.println();
        System.out.println("Model: ");
        GraphDOT.write(result, System.out); // may throw IOException!

        // Visualization.visualize(result);

        System.out.println("-------------------------------------------------------");

        // OTUtils.displayHTMLInBrowser(lstar_roca.getObservationTable().toClassicObservationTable());

        Path finalTable = outputPath.resolve("final.html");
        OTUtils.writeHTMLToFile(learningAlgorithm.getObservationTable().toClassicObservationTable(), finalTable.toFile());
        FileWriter finalModel = new FileWriter(outputPath.resolve("final.dot").toFile());
        GraphDOT.write(result, finalModel);
        finalModel.close();
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

}
