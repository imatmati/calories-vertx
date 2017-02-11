package com.loader.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by b41d3r on 11/02/17.
 */
public class LoaderVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    System.out.println("ARGS "+ Arrays.asList(args));

    String file = args[0];
    String fileType = args[1];

    Logger mongoLogger = Logger.getLogger( "org.mongodb" );
    mongoLogger.setLevel(Level.SEVERE);

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new BatchAlimentFileVerticle());

    vertx.setTimer(200, evt -> {
        final EventBus bus = vertx.eventBus();
        bus.send(fileType, file);
    });
  }

}
