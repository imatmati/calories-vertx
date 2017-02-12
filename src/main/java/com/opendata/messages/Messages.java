package com.opendata.messages;

import com.opendata.utils.DataUtils;

public interface  Messages {

  interface Adresses {
    String ALIMENT_CSV = DataUtils.ALIMENT_COLLECTION+":csv";
  }

  interface Macros {
    String CALORIE = "calculate:calorie";
  }

}
