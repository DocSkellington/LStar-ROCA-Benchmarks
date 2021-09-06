package be.ac.umons.jsonroca;

import java.util.Objects;

import net.automatalib.words.abstractimpl.AbstractSymbol;

/**
 * Special symbol used for the JSON benchmarks.
 * 
 * It simply holds a String as its underlying information.
 * 
 * @author Gaëtan Staquet
 */
public class JSONSymbol extends AbstractSymbol<JSONSymbol> {

    private final String actualSymbols;

    private JSONSymbol(String actualSymbols) {
        this.actualSymbols = actualSymbols;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        JSONSymbol o = (JSONSymbol)obj;
        return actualSymbols.equals(o.actualSymbols);
    }

    @Override
    public int compareTo(JSONSymbol other) {
        return actualSymbols.compareTo(other.actualSymbols);
    }

    public static JSONSymbol toSymbol(String string) {
        return new JSONSymbol(string);
    }

    public static JSONSymbol toSymbol(Character character) {
        return new JSONSymbol(Character.toString(character));
    }

    @Override
    public String toString() {
        return actualSymbols;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actualSymbols);
    }
    
}
