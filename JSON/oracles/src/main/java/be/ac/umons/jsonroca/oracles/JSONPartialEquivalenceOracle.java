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
package be.ac.umons.jsonroca.oracles;

import java.util.Collection;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.jsonroca.WordConversion;
import be.ac.umons.jsonschematools.DefaultGenerator;
import be.ac.umons.jsonschematools.DefaultValidator;
import be.ac.umons.jsonschematools.Generator;
import be.ac.umons.jsonschematools.GeneratorException;
import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.Validator;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

/**
 * Partial equivalence oracle for JSON documents.
 * 
 * It tests whether the provided ROCA accepts the same sets of JSON documents than the JSON Schema, up to a tree depth that depends on the counter limit.
 * 
 * @author Gaëtan Staquet
 */
public class JSONPartialEquivalenceOracle implements EquivalenceOracle.RestrictedAutomatonEquivalenceOracle<JSONSymbol> {

    private int counterLimit = 0;
    private final Generator generator;
    private final JSONSchema schema;
    private final Validator validator;
    private final int numberTests;
    private final boolean shuffleKeys;
    private final Random rand;

    public JSONPartialEquivalenceOracle(int numberTests, int maxProperties, int maxItems, JSONSchema schema, Random random, boolean shuffleKeys) {
        this.numberTests = numberTests;
        this.schema = schema;
        this.generator = new DefaultGenerator(maxProperties, maxItems);
        this.validator = new DefaultValidator();
        this.shuffleKeys = shuffleKeys;
        this.rand = random;
    }

    @Override
    public @Nullable DefaultQuery<JSONSymbol, Boolean> findCounterExample(DFA<?, JSONSymbol> hypothesis, Collection<? extends JSONSymbol> inputs) {
        for (int i = 0 ; i < numberTests ; i++) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }
            boolean correctForSchema;
            JSONObject document;
            try {
                document = (JSONObject) generator.generate(schema, counterLimit);
                correctForSchema = validator.validate(schema, document);
            } catch (GeneratorException | JSONException | JSONSchemaException e) {
                e.printStackTrace(System.err);
                return null;
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
