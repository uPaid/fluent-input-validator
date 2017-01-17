package validator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple wrapper for {@link HashMap} with custom {@link #toString()} method.
 *
 * @author Paweł Fiuk
 */

public class ValidationMap extends HashMap<String, List<String>> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting()
                                                      .create();

    public ValidationMap() {
    }

    public ValidationMap(Map<? extends String, ? extends List<String>> map) {
        super(map);
    }

    /**
     * @return JSON representation of this map.
     */
    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
