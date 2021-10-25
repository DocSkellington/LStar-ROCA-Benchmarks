# Benchmarks for Realtime One-Counter Automata Learning
This project is used to run benchmarks on the [LearnLib](https://github.com/learnlib/learnlib)'s implementation of Realtime One-Counter Automata (ROCA).
It is distributed under the Apache 2.0 License.

## Random benchmarks
The random benchmarks are straightforward: for fixed ROCA and alphabet sizes, we generate an ROCA, and run the learning algorithm on it.
Note that the random generation does not have any guarantees on the number of reachable and co-reachable states.
However, we try to maximize the number of reachable states.
Thus, the random generation is as follows:
  1. Let `n` be the ROCA size we consider and `k` be the alphabet size.
  1. We generate an ROCA `A` by constructing `n` states (each state has a probability 0.5 to be final) and the transitions between the states for the `k` symbols and the two transition functions.
  1. If the number of reachable states of `A` is `n`, we return `A`.
  1. Otherwise, we go back at 2.
  1. If after 100 tries, we could not generate an ROCA with `n` reachable states, we return an ROCA such that the number of reachable states was maximal among the ones seen.

Results are written in a CSV file in the folder `Results/Random/`.
The columns of the CSV file are as following:
  * "Target ROCA size" and "Alphabet size": the parameter for the random generation.
  * "Total time (ms)": the total time taken by the learning algorithm, in milliseconds.
  * "ROCA counterexample time (ms)" and "DFA counterexample time (ms)": the time needed to find counterexamples for the constructed ROCAs and DFAs, respectively, in milliseconds.
  * "Learning DFA time (ms)": the time needed to learn all the intermediate DFAs, in milliseconds.
  * "Table time (ms)": the time needed to make the table closed, Sigma-consistent, and bottom-consistent, in milliseconds.
  * "Finding descriptions (ms)": the time needed to compute the periodic descriptions of the the DFAs, in milliseconds.
  * "Membership queries", "Counter value queries", "Partial equivalence queries", "Equivalence queries": the number of asked queries.
  * "Rounds": the number of iterations of the main learning loops.
    Equals to the number of processed ROCAs counterexamples + 1.
  * "Openness", "Sigma-inconsistencies", "Bottom-inconsistencies": the number of resolved errors in the table.
  * "Mismatches": among the number of "Bottom-inconsistencies", gives the number of cases where a new "only for language" column was added.
  * "Length longest cex": the length of the longest counterexample, ROCA and DFA alike.
  * "|R|", "|S|", "|Ŝ \ S|": the number of short prefixes, classical suffixes (with counter values), and "only for language" suffixes (without counter values).
  * "# of bin rows": the number of short prefix rows that represent the bin state.
  * "Counter limit": the counter limit of the table at the end of the learning process.
  * "Result target size": the size of the learned ROCA.

## JSON benchmarks
The JSON-based benchmarks aim to construct an ROCA accepting the same set of JSON documents than a given JSON Schema.
In order to be able to learn a JSON Schema, some assumptions must be made:
  * Strings are equal to `"\S"`, integers to `"\I"`, and numbers to `"\D"`.
  * For every key `key` of an object, `"key"` is considered a single input symbol in the alphabet.
  * The alphabet grows in size according to the new symbols present in the ROCA and DFA counterexamples.

The resulting ROCA's transitions match with these assumptions.
That is, a JSON document must be abstracted the same way before being verified by the ROCA.

Results are written in a CSV file in the folder `Results/JSON/`.
The columns are the same than for random benchmarks except that "Target ROCA size" and "Alphabet size" are removed, and "Result alphabet size" is added.

## Build instructions
First, follow the build instructions from [AutomataLib](https://github.com/DocSkellington/automatalib) (at least version 0.11) and [LearnLib](https://github.com/DocSkellington/learnlib) (at least version 0.17).
Run `mvn clean install` in the `JSON` directory to locally install the JSON-based part. Then, run `mvn clean package` in the `benchmarks` directory.
A `jar` file including the dependencies will be produced in `benchmarks/target`.

## Running random benchmarks
To run benchmarks based on randomly generated ROCAs, run `java -jar benchmarks/target/rocabenchmarks-1.0-jar-with-dependencies.jar random {Timeout} {minROCASize} {maxROCASize} [minAlphabetSize [maxAlphabetSize [numberRepetitions]]]` where:
  * `{Timeout}` is the time limit in seconds allowed for each learning process (i.e., for each generated ROCA). Mandatory argument.
  * `{minROCASize}` is the minimal ROCA size to consider for the random generation. Mandatory argument.
  * `{maxROCASize}` is the maximal ROCA size to consider for the random generation. Mandatory argument.
  * `minAlphabetSize` is the minimal alphabet size to consider for the random generation. Optional argument. Default value: 3.
  * `maxAlphabetSize` is the maximal alphabet size to consider for the random generation. Optional argument. Default value: 3.
  * `nRepetitions` is the number of ROCAs to generate by combination of ROCA size and alphabet size. Optional argument. Default value: 3.


## Running JSON benchmarks
To run benchmarks based on JSON Schemas, run `java -jar benchmarks/target/rocabenchmarks-1.0-jar-with-dependencies.jar json {Timeout} {filePath} [nTests [nRepetitions [shuffleKeys]]]` where:
  * `{Timeout}` is the time limit in seconds allowed for each learning process (i.e., for each generated ROCA). Mandatory argument.
  * `{filePath}` is the path to the JSON Schema.
  * `nTests` is the number of tests to execute in the (partial) equivalence oracles. Default value: 1000.
  * `nRepetitions` is the number of times the experiment must be repeated. Default value: 1000.
  * `shuffleKeys` decides whether the keys of the objects must be shuffled.
    If false, the ROCA assumes a fixed order on the keys.
    That does not satisfy JSON Schema requirement but allows a faster learning algorithm.
    Default value: true.

## Generating statistical data
This project also includes two Python 3 scripts to generate statistical figures from the results:
  * `generate_statistics_for_random.py` for random ROCA benchmarks.
  * `generate_statistics_for_json.py` for JSON benchmarks.

These scripts require `pandas`, `numpy`, and `scipy` to be installed.

  * For random benchmarks, run `python3 generate_statistics_for_random.py {CSVFile} {Timeout}` where `{Timeout}` must be equal to the time limit used for the benchmarks.
  Two files are produced in `statistics/`.
  One is a LaTeX table (requiring `booktabs`) listing the number of timeouts and out of memory errors by pair of alphabet and target ROCA size.
  The second file contains data that can be used by `PGFPlots` to display 3D surface plots.
  * For JSON benchmarks, the command is `python3 generate_statistics_for_json.py {CSVFile} {Timeout} {Name}`.
  A single file containing a LaTeX table is produced in `statistics/{Name}.json`.
