package be.ac.umons.jsonroca.oracles;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONObject;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.jsonroca.WordConversion;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import net.jimblackler.jsongenerator.Configuration;
import net.jimblackler.jsongenerator.Generator;
import net.jimblackler.jsongenerator.JsonGeneratorException;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.ValidationException;
import net.jimblackler.jsonschemafriend.Validator;

public class JSONPartialEquivalenceOracle implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> {

    private int counterLimit = 0;
    private final Generator generator;
    private final Schema schema;
    private final Validator validator;
    private final int numberTests;
    private final boolean shuffleKeys;
    private final Random rand;

    public JSONPartialEquivalenceOracle(int numberTests, Schema schema, Configuration configuration, SchemaStore schemaStore, Random random, boolean shuffleKeys) throws GenerationException {
        this.numberTests = numberTests;
        this.schema = schema;
        this.generator = new Generator(configuration, schemaStore, random);
        this.validator = new Validator();
        this.shuffleKeys = shuffleKeys;
        this.rand = random;
    }

    public JSONPartialEquivalenceOracle(int numberTests, Schema schema, SchemaStore schemaStore, Random random, boolean shuffleKeys) throws GenerationException {
        this(numberTests, schema, new DefaultGeneratorConfiguration(), schemaStore, random, shuffleKeys);
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(DFA<?, JSONSymbol> hypothesis, Collection<? extends JSONSymbol> inputs) {
        for (int i = 0 ; i < numberTests ; i++) {
            boolean correctForSchema;
            JSONObject document = null;
            try {
                document = (JSONObject) generator.generate(schema, counterLimit);
                validator.validate(schema, document);
                correctForSchema = true;
            } catch (JsonGeneratorException e) {
                e.printStackTrace();
                return null;
            } catch (ValidationException e) {
                correctForSchema = false;
            }

            Word<JSONSymbol> word = WordConversion.fromJSONDocumentToJSONSymbolWord(document, shuffleKeys, rand);
            boolean correctForHypo = hypothesis.accepts(word);

            if (correctForSchema != correctForHypo) {
                return new DefaultQuery<>(word, correctForSchema);
            }
        }
        return null;
    }

    @Override
    public void setCounterLimit(int counterLimit) {
        this.counterLimit = counterLimit;
    }
    
}
