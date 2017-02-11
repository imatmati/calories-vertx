package com.opendata.utils;

import java.util.Optional;


public class FileUtils {

  public static final String DOT_SEPARATOR = ".";
  public static final String CSV_SEPARATOR = ";";
  public static final String LINE_SEPARATOR = "\n";

  public static Optional<String> getFileExtension(String file) {
    if (file == null) {
      return Optional.empty();
    }

    int extIndex = file.lastIndexOf(DOT_SEPARATOR);

    if (extIndex == -1) {
      return Optional.empty();
    } else {
      return Optional.of(file.substring(extIndex + 1));
    }
  }
}
