/*
Copyright 2021 University of Mons and University of Antwerp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package be.ac.umons.rocabenchmarks;

import java.util.Random;

import net.automatalib.automata.oca.ROCA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;

/**
 * Generates a random ROCA.
 * 
 * The generation does not guarantee the number of reachable and co-reachable states.
 * 
 * @author Gaëtan Staquet
 */
public class RandomROCA<I> {
    private final Alphabet<I> alphabet;
    private final ROCA<?, I> roca;

    public RandomROCA(Random rand, Alphabet<I> alphabet, int size, double acceptanceProbability, int nTries) {
        this.alphabet = alphabet;
        ROCA<?, I> potentialROCA = RandomAutomata.randomROCA(rand, size, acceptanceProbability, alphabet);
        while (potentialROCA.numberOfReachableLocations() != size && nTries-- != 0) {
            ROCA<?, I> r = RandomAutomata.randomROCA(rand, size, acceptanceProbability, alphabet);
            if (r.numberOfReachableLocations() > potentialROCA.numberOfReachableLocations()) {
                potentialROCA = r;
            }
        }
        roca = potentialROCA;
    }

    public RandomROCA(Alphabet<I> alphabet, int size, double acceptanceProbability, int nTries) {
        this(new Random(), alphabet, size, acceptanceProbability, nTries);
    }

    public ROCA<?, I> getROCA() {
        return roca;
    }

    public Alphabet<I> getAlphabet() {
        return alphabet;
    }
}
