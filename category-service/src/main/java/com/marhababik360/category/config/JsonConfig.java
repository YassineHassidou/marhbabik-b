package com.marhababik360.category.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonConfig {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private JsonConfig() {}
    public static Gson gson() { return GSON; }
}
