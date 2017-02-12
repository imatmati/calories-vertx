package com.opendata.loader.verticles;

import com.opendata.utils.DataUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.mongo.MongoClient;

import java.nio.charset.Charset;
import java.util.Objects;

import static com.opendata.messages.Messages.Adresses;
import static com.opendata.utils.FileUtils.CSV_SEPARATOR;
import static com.opendata.utils.FileUtils.LINE_SEPARATOR;

public class BatchAlimentCSVFileVerticle extends AbstractVerticle {

   EventBus bus;

  @Override
  public void start() throws Exception {

    bus = vertx.eventBus();
    bus.consumer(Adresses.ALIMENT_CSV, this::loadFile);

  }

  private void loadFile(Message<String> tMessage) {
    String filePath = tMessage.body();

    JsonObject mongoconfig = new JsonObject()
      .put("connection_string",  "mongodb://localhost:27017")
      .put("db_name", "opendigital");

    MongoClient mongoClient = MongoClient.createShared(vertx, mongoconfig);
    mongoClient.dropCollection("aliments",evt->{
      FileSystem fs = vertx.fileSystem();
      fs.open(filePath, new OpenOptions(), fd -> {
        if (!fd.failed()) {
          AsyncFile file = fd.result();
          file.endHandler(event -> {
                    bus.close((e) -> {});
                    vertx.close();});
          Pump pump = Pump.pump(file, new CSVWriter(mongoClient));
          pump.start();

        } else {
          fd.cause().printStackTrace();
        }
      });
    });

  }


  private static class CSVWriter implements WriteStream<Buffer> {

    private MongoClient mongoClient;

    private boolean isFirstBuffer =true;

    public CSVWriter(MongoClient mongoClient) {

      this.mongoClient = mongoClient;
    }


    @Override
    public WriteStream write(Buffer data) {

      //Encodage particulier du fichier à traiter
      String[] rowData = data.toString(Charset.forName("ISO-8859-1")).split(LINE_SEPARATOR);
      // Pour passer la ligne de headers
      int j =0;
      if (isFirstBuffer) {
        j=1;
        isFirstBuffer = !isFirstBuffer;
      }

      for (; j < rowData.length; j++) {

        String[] cols = rowData[j].split(CSV_SEPARATOR);
        if (incompletePreviousLine.length() != 0) {

          cols = (incompletePreviousLine + rowData[j]).split(CSV_SEPARATOR);
          incompletePreviousLine = "";
        }
        int leftFields = cols.length % NB_FIELDS;

        if (leftFields > 0) {
          incompletePreviousLine = rowData[j];
        } else {
          JsonObject obj = DataUtils.getJsonObjectFrom(cols, FIELDS);
          // Pas d'insert many disponible ...
          if (Objects.nonNull(obj)) {
            mongoClient.save(DataUtils.ALIMENT_COLLECTION, obj, id -> {
            });
          }
        }
      }
      return this;
    }



    @Override
    public void end(){
    }

    @Override
    public WriteStream setWriteQueueMaxSize(int maxSize) {
      return this;
    }

    @Override
    public boolean writeQueueFull() {
      return false;
    }

    @Override
    public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
      return null;
    }

    @Override
    public WriteStream exceptionHandler(Handler<Throwable> handler) {
      return this;
    }

    private String incompletePreviousLine = "";
    private String[] FIELDS = {"ORIGGPCD",
      "ORIGGPFR",
      "ORIGFDCD",
      "ORIGFDNM",
      "energie1",
      "energie2",
      "energie3",
      "energie4",
      "eau",
      "proteines",
      "proteines_brutes",
      "glucides",
      "lipides",
      "sucres",
      "amidon",
      "fibres_alimentaires",
      "polyols_totaux",
      "cendres",
      "alcool",
      "acides_organiques",
      "ag_satures",
      "ag_monoinsatures",
      "ag_polyinsatures",
      "ag4,_butyrique",
      "ag6_caproique",
      "ag8_caprylique",
      "ag10_caprique",
      "ag12_laurique",
      "ag14_myristique",
      "ag16_palmitique",
      "ag18_stearique",
      "ag18_1",
      "ag18_2",
      "ag18_3",
      "ag20_4",
      "ag20_5",
      "ag22_6",
      "cholesterol",
      "sel",
      "calcium",
      "chlorure",
      "cuivre",
      "fer",
      "iode",
      "magnesium",
      "manganese",
      "phosphore",
      "potassium",
      "selenium",
      "sodium",
      "zinc",
      "retinol",
      "beta-carotene",
      "vitamine_d",
      "vitamine_e",
      "vitamine_k1",
      "vitamine_k2",
      "vitamine_c",
      "vitamine_b1",
      "vitamine_b2",
      "vitamine_b3",
      "vitamine_b5",
      "vitamine_b6",
      "vitamine_b9",
      "vitamine_b12"};

    private final int NB_FIELDS = FIELDS.length;
  }
}
