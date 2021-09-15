package be.ac.umons.jsonroca.oracles;

import org.json.JSONException;
import org.json.JSONObject;

import be.ac.umons.jsonroca.JSONSymbol;
import be.ac.umons.jsonroca.WordConversion;
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
    public Boolean answerQuery(Word<JSONSymbol> input) {
        String string = WordConversion.fromJSONSymbolWordToString(input);
        if (!Utils.validWord(string)) {
            return false;
        }
        string = escapeSymbolsForJSON(string);
        JSONObject json;
        try {
            json = new JSONObject(string);
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

    @Override
    public Boolean answerQuery(Word<JSONSymbol> prefix, Word<JSONSymbol> suffix) {
        return answerQuery(prefix.concat(suffix));
    }

    public static String escapeSymbolsForJSON(String string) {
        // We escape the "\S", "\I", and "\D" symbols in the document (to avoid errors from JSONObject)
        // That means we replace every \\([SID]) by \\\\$1. We need to escape each \ in the Java code
        // We also want to replace only the symbols in the values (not in the keys). So, we add a positive look-ahead check
        return string.replaceAll("\\\\([SID])((?=[,\\]}\\s])|(?=\"[,}\\]]))", "\\\\\\\\$1");
    }
}
