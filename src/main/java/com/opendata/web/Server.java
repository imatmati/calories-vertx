package com.opendata.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by b41d3r on 06/02/17.
 */
public class Server extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    super.start();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // Create a router endpoint for the static content.
    router.route().handler(StaticHandler.create());
    vertx.createHttpServer().requestHandler(req -> {
      req.setExpectMultipart(false);
      req.bodyHandler(buffer -> {
        try {
          int size = buffer.length() / (1024 * 1024);
          Files.write(Paths.get("c:/bulk.txt"), buffer.getBytes());
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }).listen(8080);
    }
  }
