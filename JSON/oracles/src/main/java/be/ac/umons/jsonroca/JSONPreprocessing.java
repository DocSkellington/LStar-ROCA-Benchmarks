package be.ac.umons.jsonroca;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONPreprocessing {
    public static JSONObject prepareDocument(JSONObject document) {
        return processObject(document);
    }

    private static JSONObject processObject(JSONObject objectToProcess) {
        JSONObject preprocessed = new JSONObject();
        for (String key : objectToProcess.keySet()) {
            Object object = objectToProcess.get(key);
            if (object instanceof JSONObject) {
                JSONObject subObject = processObject((JSONObject) object);
                preprocessed.put(key, subObject);
            }
            else if (object instanceof JSONArray) {
                JSONArray array = processArray((JSONArray) object);
                preprocessed.put(key, array);
            }
            else {
                Object value = processValue(object);
                preprocessed.put(key, value);
            }
        }

        return preprocessed;
    }

    private static JSONArray processArray(JSONArray arrayToProcess) {
        JSONArray preprocessed = new JSONArray();
        for (Object object : arrayToProcess) {
            if (object instanceof JSONObject) {
                preprocessed.put(processObject((JSONObject) object));
            }
            else if (object instanceof JSONArray) {
                preprocessed.put(processArray((JSONArray) object));
            }
            else {
                preprocessed.put(processValue(object));
            }
        }
        return preprocessed;
    }

    private static Object processValue(Object object) {
        if (object instanceof String) {
            return "\\A";
        }
        else {
            return object;
        }
    }
}
