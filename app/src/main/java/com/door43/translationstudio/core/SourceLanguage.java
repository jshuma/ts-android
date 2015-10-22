package com.door43.translationstudio.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a language from which a project is translated
 */
public class SourceLanguage implements Comparable {
    public final String code;
    public final String name;
    public final LanguageDirection direction;
    public final int dateModified;
    public final String projectTitle;
    public final String projectDescription;
    public final String resourceCatalog;

    public SourceLanguage(String code, String name, int dateModified, LanguageDirection direction, String projectTitle, String projectDescription, String resourceCatalog) {
        this.code = code;
        this.name = name;
        this.dateModified = dateModified;
        this.direction = direction;
        this.projectTitle = projectTitle;
        this.projectDescription = projectDescription;
        this.resourceCatalog = resourceCatalog;
    }

    public String getId() {
        return code;
    }

    public LanguageDirection getDirection() {
        return direction;
    }

    /**
     * Generates a source language object from json
     * @param json
     * @return
     * @throws JSONException
     */
    public static SourceLanguage generate(JSONObject json) throws JSONException {
        if(json == null) {
            return null;
        }
        JSONObject projectJson = json.getJSONObject("project");
        String projectDescription = "";
        if(projectJson.has("desc")) {
            projectDescription = projectJson.getString("desc");
        }
        return new SourceLanguage(
                json.getString("slug"),
                json.getString("name"),
                json.getInt("date_modified"),
                LanguageDirection.get(json.getString("direction")),
                projectJson.getString("name"),
                projectDescription,
                json.getString("res_catalog"));
    }

    @Override
    public int compareTo(Object another) {
        String anotherCode = ((SourceLanguage)another).getId();
        return code.compareToIgnoreCase(anotherCode);
    }
}
