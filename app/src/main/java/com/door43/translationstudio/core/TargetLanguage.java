package com.door43.translationstudio.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a language to which a project is translated
 */
public class TargetLanguage implements Comparable {

    public final LanguageDirection direction;
    public final String region;
    public final String name;
    public final String code;

    /**
     * Returns the language code for the target language
     * @return
     */
    public String getId() {
        return code;
    }

    @Override
    public int compareTo(Object another) {
        String anotherCode = ((TargetLanguage)another).getId();
        return code.compareToIgnoreCase(anotherCode);
    }

    public TargetLanguage (String code, String name, String region, LanguageDirection direction) {
        this.code = code;
        this.name = name;
        this.region = region;
        this.direction = direction;
    }

    /**
     * Generates a new target language from json
     * @param json
     * @return
     */
    public static TargetLanguage generate(JSONObject json) throws JSONException {
        if(json == null) {
            return null;
        }
        return new TargetLanguage(
                json.getString("lc"),
                json.getString("ln"),
                json.getString("lr"),
                LanguageDirection.get(json.getString("ld"))
        );
    }

    public LanguageDirection getDirection() {
        return direction;
    }
}
