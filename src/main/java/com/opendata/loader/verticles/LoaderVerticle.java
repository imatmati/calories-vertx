package com.opendata.loader.verticles;

import com.opendata.utils.FileUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.util.logging.Level;
import java.util.logging.Logger;


public class LoaderVerticle extends AbstractVerticle {

  public static void main(String[] args) {

    new LoaderVerticle().execute(args);
  }

  private void execute(String[] args) throws IllegalArgumentException {

    if (args.length != 2) {
      printUsage();
      System.exit(-1);
    }

    String file = args[0],
      dataType = args[1];
    String extension = FileUtils.getFileExtension(file).orElseThrow(() -> new IllegalArgumentException("file " + file + " has no known extension"));
    generalConfiguration();

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new BatchAlimentCSVFileVerticle(),evt -> {
      final EventBus bus = vertx.eventBus();
      bus.send(dataType + ":" + extension, file);
    });

  }

  private void printUsage() {
    System.err.println("java <class> <filename> <file type>");
  }

  private void generalConfiguration() {
    Logger mongoLogger = Logger.getLogger("org.mongodb");
    mongoLogger.setLevel(Level.SEVERE);
  }
}
