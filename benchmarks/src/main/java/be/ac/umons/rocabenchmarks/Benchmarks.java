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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;

/**
 * Main class for the benchmarks.
 * 
 * See README.md for instructions.
 * 
 * @author Gaëtan Staquet
 */
public class Benchmarks {
    public static void main(String[] args) throws InterruptedException, IOException, GenerationException {
        final String type = args[0];
        final int timeLimit = Integer.valueOf(args[1]);

        final Duration timeout = Duration.ofSeconds(timeLimit);
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        final LocalDateTime now = LocalDateTime.now();

        final Random rand = new Random();
        if (type.equals("random")) {
            final int minSize = Integer.valueOf(args[2]);
            final int maxSize = Integer.valueOf(args[3]);
            int minAlphabetSize = 3;
            int maxAlphabetSize = 3;
            int nRepetitions = 1000;
            if (args.length >= 6) {
                minAlphabetSize = Integer.valueOf(args[4]);
                maxAlphabetSize = Integer.valueOf(args[5]);

                if (args.length >= 7) {
                    nRepetitions = Integer.valueOf(args[6]);
                }
            }
            System.out.println("Starting random benchmarks with the following parameters:");
            System.out.println("Size of ROCAs between " + minSize + " and " + maxSize);
            System.out.println("Size of alphabets between " + minAlphabetSize + " and " + maxAlphabetSize);
            System.out.println("Number of ROCAs by sizes " + nRepetitions);

            Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "Random");
            pathToCSVFolder.toFile().mkdirs();
            Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-" + minSize + "-" + maxSize + "-" + minAlphabetSize + "-" + maxAlphabetSize + "-" + nRepetitions + "-" + dtf.format(now) + ".csv");

            RandomBenchmarks randomBenchmarks = new RandomBenchmarks(pathToCSVFile, timeout);
            randomBenchmarks.runBenchmarks(rand, minSize, maxSize, minAlphabetSize, maxAlphabetSize, nRepetitions);
        }
        else if (type.toLowerCase().equals("json")) {
            final Path filePath = Paths.get(args[2]);
            final String schemaName = filePath.getFileName().toString();
            int nTests = 1000;
            int nRepetitions = 1000;
            boolean shuffleKeys = true;
            if (args.length >= 4) {
                nTests = Integer.valueOf(args[3]);
                if (args.length >= 5) {
                    nRepetitions = Integer.valueOf(args[4]);
                    if (args.length >= 6) {
                        shuffleKeys = Boolean.valueOf(args[5]);
                    }
                }
            }

            SchemaStore schemaStore = new SchemaStore();
            Schema schema;
            try {
                URL url = filePath.toUri().toURL();
                schema = schemaStore.loadSchema(url.toURI(), false);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            System.out.println("Starting JSON benchmarks with the following parameters:");
            System.out.println("Schema name: " + schemaName + "; schema contents: " + schema);
            System.out.println("Number of tests for equivalence: " + nTests);
            System.out.println("Number of repetitions: " + nRepetitions);

            Path pathToCSVFolder = Paths.get(System.getProperty("user.dir"), "Results", "JSON");
            pathToCSVFolder.toFile().mkdirs();
            Path pathToCSVFile = pathToCSVFolder.resolve("" + timeLimit + "s-" + schemaName + "-" + nTests + "-" + nRepetitions + "-" + shuffleKeys + "-" + dtf.format(now) + ".csv");
            JSONBenchmarks jsonBenchmarks = new JSONBenchmarks(pathToCSVFile, timeout);
            jsonBenchmarks.runBenchmarks(rand, schema, schemaName, schemaStore, nTests, nRepetitions, shuffleKeys);
        }
    }
}
