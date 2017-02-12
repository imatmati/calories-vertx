package com.opendata.macro.verticles;

import com.opendata.messages.Messages.Macros;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CalculateCalories extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    super.start();
    EventBus bus = vertx.eventBus();
    bus.consumer(Macros.CALORIE, this::calculateCalorie);
  }

  private void calculateCalorie(Message<JsonArray> tMessage) {

    JsonArray aliments = tMessage.body();
    for (Object data : aliments) {
      JsonObject aliment = (JsonObject) data;
      double proteines = Double.parseDouble(aliment.getString("proteines"));
      double glucides = Double.parseDouble(aliment.getString("glucides"));
      double lipides = Double.parseDouble(aliment.getString("lipides"));
      double calories = proteines*4 + glucides *4 + lipides *9;
      aliment.put("calories", calories);
    }
    tMessage.reply(aliments);

  }


}
