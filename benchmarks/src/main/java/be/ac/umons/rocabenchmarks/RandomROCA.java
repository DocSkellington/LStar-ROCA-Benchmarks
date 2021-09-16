package be.ac.umons.rocabenchmarks;

import java.util.Random;

import net.automatalib.automata.oca.ROCA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;

public class RandomROCA<I> {
    private final Alphabet<I> alphabet;
    private final ROCA<?, I> roca;

    public RandomROCA(Random rand, Alphabet<I> alphabet, int size, double acceptanceProbability, int nTries) {
        this.alphabet = alphabet;
        ROCA<?, I> potentialROCA = RandomAutomata.randomROCA(rand, size, acceptanceProbability, alphabet);
        while (potentialROCA.numberOfReachableLocations() != size && nTries-- != 0) {
            System.out.println(nTries);
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
