package be.ac.umons.rocabenchmarks.oracles;

import net.jimblackler.jsongenerator.Configuration;

public class DefaultGeneratorConfiguration implements Configuration {
    @Override
    public boolean isPedanticTypes() {
        return true;
    }

    @Override
    public boolean isGenerateNulls() {
        return false;
    }

    @Override
    public boolean isGenerateMinimal() {
        return false;
    }

    @Override
    public float nonRequiredPropertyChance() {
        return 0.5f;
    }
}
