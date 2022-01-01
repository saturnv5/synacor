package com.google.synacor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
  private static final String FILE_PATH = "./res/challenge.bin";
  public static void main(String[] args) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(FILE_PATH));
    new Machine(System.in, System.out, bytes).execute();
  }
}
