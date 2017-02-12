package com.opendata.web;

import com.opendata.macro.verticles.CalculateCalories;
import com.opendata.utils.DataUtils;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

import static com.opendata.messages.Messages.Macros;

;

/**
 * Created by b41d3r on 06/02/17.
 */
public class Server extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Server());
    vertx.deployVerticle(new CalculateCalories(), new DeploymentOptions().setWorker(true));
  }

  private MongoClient mongo;

  @Override
  public void start() throws Exception {
    super.start();
    JsonObject mongoconfig = new JsonObject()
      .put("connection_string",  "mongodb://localhost:27017")
      .put("db_name", "opendigital");
    mongo = MongoClient.createShared(vertx, mongoconfig);
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.get("/api/aliments").handler(getAliments());

    router.get("/api/aliments/names").handler(getAlimentNames());

    router.get("/api/aliments/:id").handler(getAlimentById());

    router.get("/api/aliments/calorie/:id").handler(calculateCaloriesById());
    router.route();

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private Handler<RoutingContext> calculateCaloriesById() {
    EventBus bus = vertx.eventBus();
    return ctx -> {
      mongo.findOne(DataUtils.ALIMENT_COLLECTION, new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
        if (lookup.failed()) {
          ctx.fail(500);
          return;
        }
        JsonObject result = lookup.result();
        JsonArray aliments = new JsonArray().add(result);

        Handler<AsyncResult<Message<JsonArray>>> replyCaloriesHandler= event -> {

          if (event.failed()) {
            ctx.fail(500);
            return;
          }

          // Pour tests
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "accept, authorization, content-type, email");

          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
          ctx.response().end(event.result().body().encode());

        };

        bus.send(Macros.CALORIE, aliments,replyCaloriesHandler );


      });
    };
  }

  private Handler<RoutingContext> getAliments() {
    return ctx -> {
      mongo.find(DataUtils.ALIMENT_COLLECTION, new JsonObject(), getListAliments(ctx));
    };
  }

  private Handler<RoutingContext> getAlimentById() {
    return ctx -> {
        mongo.findOne(DataUtils.ALIMENT_COLLECTION, new JsonObject().put("_id", ctx.request().getParam("id")), null, lookup -> {
          if (lookup.failed()) {
            ctx.fail(500);
            return;
          }
          // Pour tests
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
          ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "accept, authorization, content-type, email");

          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
          ctx.response().end(lookup.result().encode());
        });
      };
  }

  private Handler<RoutingContext> getAlimentNames() {
    return ctx -> {
      mongo.findWithOptions(DataUtils.ALIMENT_COLLECTION, new JsonObject(),
        new FindOptions().setFields(new JsonObject().put("ORIGFDNM",true)), getListAliments(ctx));
    };
  }

  private Handler<AsyncResult<List<JsonObject>>> getListAliments(RoutingContext ctx) {
    return lookup -> {

      if (lookup.failed()) {
        ctx.fail(500);
        return;
      }

      final JsonArray json = new JsonArray();

      lookup.result().forEach(json::add);

      // Pour tests
      ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
      ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");
      ctx.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "accept, authorization, content-type, email");

      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      ctx.response().end(json.encode());


    };
  }
}
