package be.ac.umons.rocabenchmarks;

import java.util.Random;

import net.automatalib.automata.oca.ROCA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;

public class RandomROCA<I> {
    private final Alphabet<I> alphabet;
    private final ROCA<?, I> roca;

    public RandomROCA(Random rand, Alphabet<I> alphabet, int size, double acceptanceProbability) {
        this.alphabet = alphabet;
        this.roca = RandomAutomata.randomROCA(rand, size, acceptanceProbability, alphabet);
    }

    public RandomROCA(Alphabet<I> alphabet, int size, double acceptanceProbability) {
        this(new Random(), alphabet, size, acceptanceProbability);
    }

    public ROCA<?, I> getROCA() {
        return roca;
    }

    public Alphabet<I> getAlphabet() {
        return alphabet;
    }
}
