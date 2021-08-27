package be.ac.umons.rocabenchmarks.oracles;

import org.json.JSONException;
import org.json.JSONObject;

import de.learnlib.api.oracle.SingleQueryOracle.SingleQueryOracleROCA;
import net.automatalib.words.Word;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.ValidationException;
import net.jimblackler.jsonschemafriend.Validator;

public class JSONMembershipOracle implements SingleQueryOracleROCA<Character> {

    private final Schema schema;
    private final Validator validator;

    public JSONMembershipOracle(Schema schema) {
        this.schema = schema;
        this.validator = new Validator();
    }

    @Override
    public Boolean answerQuery(Word<Character> prefix, Word<Character> suffix) {
        Word<Character> word = prefix.concat(suffix);
        if (!Utils.validWord(word)) {
            return false;
        }
        JSONObject json;
        try {
            json = new JSONObject(Utils.wordToString(word));
        }
        catch (JSONException e) {
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
