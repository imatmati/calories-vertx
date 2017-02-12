package com.opendata.web;

import com.opendata.macro.verticles.CalculateCalories;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Server extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Server());
  }


  @Override
  public void start() throws Exception {
    super.start();
    // Verticle Worker pour travailler sur thread séparé de la boucle.
    vertx.deployVerticle(new CalculateCalories(), new DeploymentOptions().setWorker(true));
    JsonObject mongoconfig = new JsonObject()
      .put("connection_string",  "mongodb://localhost:27017")
      .put("db_name", "opendigital");
    MongoClient mongo = MongoClient.createShared(vertx, mongoconfig);
    Router router = Router.router(vertx);


    router.route().handler(BodyHandler.create());

    router.get("/api/aliments/names").handler(WebHandlers.getAlimentNames(vertx, mongo));

    router.get("/api/aliments/:id").handler(WebHandlers.getAlimentById(vertx, mongo));

    router.get("/api/aliments/calorie/:id").handler(WebHandlers.calculateCaloriesById(vertx, mongo));
    //router.route();
    router.route().handler(StaticHandler.create());
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }


}
