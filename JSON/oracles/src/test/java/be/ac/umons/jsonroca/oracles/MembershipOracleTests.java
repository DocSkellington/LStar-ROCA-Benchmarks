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

import static be.ac.umons.jsonroca.JSONSymbol.toSymbol;
import static be.ac.umons.jsonroca.JSONSymbol.toWord;

import java.net.URISyntaxException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import be.ac.umons.jsonroca.JSONSymbol;
import net.automatalib.words.Word;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;


/**
 * @author Gaëtan Staquet
 */
public class MembershipOracleTests {
    private JSONMembershipOracle createOracle(URL schemaURL) throws GenerationException, URISyntaxException {
        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.loadSchema(schemaURL.toURI(), false);
        return new JSONMembershipOracle(schema);
    }

    @Test
    public void testEscape() {
        String document = "{\"object\":{\"str1\": \"\\S\", \"in\\t\": \\I}, \"\\D\": \\D}";
        String escaped = JSONMembershipOracle.escapeSymbolsForJSON(document);
        Assert.assertEquals(escaped, "{\"object\":{\"str1\": \"\\\\S\", \"in\\t\": \\\\I}, \"\\D\": \\\\D}");
    }

    @Test
    public void testSimpleStringSchema() throws GenerationException, URISyntaxException {
        URL schemaURL = getClass().getResource("/singleString.json");
        JSONMembershipOracle oracle = createOracle(schemaURL);

        Word<JSONSymbol> word = Word.epsilon();
        Assert.assertFalse(oracle.answerQuery(word));

        word = Word.fromSymbols(toSymbol("{"), toSymbol("}"));
        Assert.assertFalse(oracle.answerQuery(word));

        word = toWord("{", "\"string\":", "\"\\S\"", "}");
        Assert.assertTrue(oracle.answerQuery(word));
    }

    @Test
    public void testNumbersSchema() throws GenerationException, URISyntaxException {
        URL schemaURL = getClass().getResource("/numbers.json");
        JSONMembershipOracle oracle = createOracle(schemaURL);
        
        Word<JSONSymbol> word = toWord("{", "\"integer\":", "\"\\I\"", "}");
        Assert.assertFalse(oracle.answerQuery(word));

        word = toWord("{", "\"integer\":", "\"\\I\"", ",", "\"double\":", "\"\\D\"", "}");
        Assert.assertTrue(oracle.answerQuery(word));
    }
}
