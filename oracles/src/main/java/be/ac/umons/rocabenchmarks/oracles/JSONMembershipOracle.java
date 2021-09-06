package be.ac.umons.rocabenchmarks.oracles;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.rocabenchmarks.JSONSymbol;
import be.ac.umons.rocabenchmarks.WordConversion;
import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryOracleROCA;
import net.automatalib.words.Word;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.ValidationException;
import net.jimblackler.jsonschemafriend.Validator;

public class JSONMembershipOracle implements SingleQueryOracleROCA<JSONSymbol> {

    private final Schema schema;
    private final Validator validator;

    public JSONMembershipOracle(Schema schema) {
        this.schema = schema;
        this.validator = new Validator();
    }

    @Override
    public Boolean answerQuery(Word<JSONSymbol> prefix, Word<JSONSymbol> suffix) {
        Word<JSONSymbol> word = prefix.concat(suffix);
        String string = WordConversion.fromJSONSymbolWordToString(word);
        if (!Utils.validWord(string)) {
            return false;
        }
        JSONObject json;
        try {
            // We escape the "\A" symbols in the document (to avoid errors from JSONObject)
            json = new JSONObject(string.replace("\\A", "\\\\A"));
        } catch (JSONException e) {
            return false;
        }

        try {
            validator.validate(schema, json);
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }
}
