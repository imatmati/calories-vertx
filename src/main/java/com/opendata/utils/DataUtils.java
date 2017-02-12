package com.opendata.utils;

import io.vertx.core.json.JsonObject;

/**
 * Created by b41d3r on 11/02/17.
 */
public class DataUtils {

  public static final String ALIMENT_COLLECTION = "aliments";
  public static final String COLONNE_NOM_ALIMENT = "ORIGFDNM";

   public static JsonObject getJsonObjectFrom(String[] row, String[] fields) {
    JsonObject jsonObject = new JsonObject();
    for (int i = 0; i < fields.length; i++) {
      String col = row[i].trim();
      if (!col.isEmpty()) {
        jsonObject.put(fields[i], col);
      }

    }
    return jsonObject;
  }
}
