package com.opendata.web;

import com.opendata.messages.Messages.Macros;
import com.opendata.utils.DataUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static com.opendata.utils.DataUtils.COLONNE_NOM_ALIMENT;

public class WebHandlers {

  public static Handler<RoutingContext> calculateCaloriesById(Vertx vertx, MongoClient mongo) {
    EventBus bus = vertx.eventBus();
    return ctx -> {
      mongo.findOne(DataUtils.ALIMENT_COLLECTION, new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          lookup.cause().printStackTrace();
          ctx.fail(500);
          return;
        }
        JsonObject result = lookup.result();
        JsonArray aliments = new JsonArray().add(result);

        Handler<AsyncResult<Message<JsonArray>>> replyCaloriesHandler= event -> {

          if (event.failed()) {
            event.cause().printStackTrace();
            ctx.fail(500);
            return;
          }

          // Pour tests
          setCors(ctx);

          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
          ctx.response().end(event.result().body().encode());

        };

        bus.send(Macros.CALORIE, aliments,replyCaloriesHandler );


      });
    };
  }

  public static Handler<RoutingContext> getAlimentById(Vertx vertx, MongoClient mongo) {
    return ctx -> {
      mongo.findOne(DataUtils.ALIMENT_COLLECTION, new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }
        // Pour tests
        setCors(ctx);

        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        ctx.response().end(lookup.result().encode());
      });
    };
  }

  public static void setCors(RoutingContext ctx) {
    ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
    ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "accept, authorization, content-type, email");
  }

  public static Handler<RoutingContext> getAlimentNames(Vertx vertx, MongoClient mongo) {
    return ctx -> {
      mongo.findWithOptions(DataUtils.ALIMENT_COLLECTION, new JsonObject(),
        new FindOptions().setFields(new JsonObject().put(COLONNE_NOM_ALIMENT,true)), getListAliments(ctx));
    };
  }

  public static Handler<AsyncResult<List<JsonObject>>> getListAliments(RoutingContext ctx) {
    return lookup -> {

      if (lookup.failed()) {
        ctx.fail(500);
        return;
      }

      final JsonArray json = new JsonArray();

      lookup.result().forEach(json::add);

      // Pour tests
      setCors(ctx);

      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      ctx.response().end(json.encode());


    };
  }
}
