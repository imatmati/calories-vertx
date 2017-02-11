package com.loader.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by b41d3r on 07/02/17.
 */
public class BatchAlimentFileVerticle extends AbstractVerticle {

    public static final String ALIMENT_FILE_TYPE = "aliments";
    @Override
    public void start() throws Exception {

      final EventBus bus = vertx.eventBus();
      bus.consumer(ALIMENT_FILE_TYPE,this::loadFile);
/*
        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "opendigital");

        MongoClient mongoClient = MongoClient.createShared(vertx, mongoconfig);
        FileSystem fs = vertx.fileSystem();
        fs.open(FILE, new OpenOptions(), fd -> {
            if(!fd.failed()) {
                AsyncFile file = fd.result();
                Pump pump = Pump.pump(file, new CustomWriter(mongoClient));
                pump.start();

            }
            else {
                fd.cause().printStackTrace();
            }
        });
        */
    }

  private  void loadFile(Message<String> tMessage) {
    String filePath = tMessage.body();
    System.out.println("FilePath "+filePath);
  }




    private static class CustomWriter implements WriteStream<Buffer> {


        private MongoClient mongoClient;
        private Handler drainHandler;
        private final int NB_FIELDS = 65;

        public CustomWriter(MongoClient mongoClient) {

            this.mongoClient = mongoClient;
        }

        String remains = "";

        @Override
        public WriteStream write(Buffer data) {

            String [] rowData = data.toString().split("\n");
            for (int j = 0 ; j< rowData.length;j++) {

                String[] cols =rowData[j].split(";");
                if (remains.length() != 0) {

                    cols=(remains+rowData[j]).split(";");
                    remains = "";
                }
                int leftFields = cols.length % NB_FIELDS;

                if (leftFields>0) {
                    remains= rowData[j];
                }
                else {
                    JsonObject obj = getJsonObjectFrom (cols);
                    // Pas d'insert many disponible ...
                    mongoClient.save("aliments",obj,id -> { });
                }
            }
            return this;
        }

        private JsonObject getJsonObjectFrom(String[] row) {

            JsonObject jsonObject = new JsonObject();
            for (int i = 0;i < FIELDS.length;i++) {
                String col = row[i].trim();
                if (!col.isEmpty()) {
                    jsonObject.put(FIELDS[i], col);
                }

            }
            return jsonObject;
        }


        @Override
        public void end() {
            System.out.println("================== End ===================");
        }

        @Override
        public WriteStream setWriteQueueMaxSize(int maxSize) {
            System.out.println("maxSize "+maxSize);
            return this;
        }

        @Override
        public boolean writeQueueFull() {
            System.out.println("writeQueueFull called");
            return false;
        }

        @Override
        public WriteStream drainHandler(Handler drainHandler) {
            this.drainHandler = drainHandler;
            System.out.println( "drainHandler "+drainHandler);
            return this;
        }

        @Override
        public WriteStream exceptionHandler(Handler<Throwable> handler) {

            return this;
        }

        String [] FIELDS = {"ORIGGPCD",
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
    }


}
