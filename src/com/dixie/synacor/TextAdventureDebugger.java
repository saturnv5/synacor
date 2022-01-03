package com.dixie.synacor;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextAdventureDebugger {
  public static void main(String[] args) throws IOException {
    new TextAdventureDebugger(TextAdventure.FILE_PATH).startDebugging();
  }

  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  private final Scanner scan;
  private final PipedOutputStream pipedOutput;
  private final Machine machine;

  public TextAdventureDebugger(String path) throws IOException {
    scan = new Scanner(System.in);
    PipedInputStream in = new PipedInputStream();
    pipedOutput = new PipedOutputStream(in);
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    machine = new Machine(in, System.out, bytes);
  }

  void startDebugging() throws IOException {
    executor.execute(this::startDebugChannel);
    machine.execute();
  }

  private void startDebugChannel() {
    while (scan.hasNextLine()) {
      String cmd = scan.nextLine();
      if (!tryDebugCommand(cmd)) {
        try {
          pipedOutput.write(cmd.getBytes(StandardCharsets.UTF_8));
          pipedOutput.write('\n');
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private boolean tryDebugCommand(String cmdString) {
    String[] cmd = cmdString.split(" ");
    try {
      return switch (cmd[0]) {
        case "get_reg" -> getReg(cmd[1]);
        case "get_mem" -> getMem(cmd[1]);
        case "set_reg" -> setReg(cmd[1], cmd[2]);
        case "set_mem" -> setMem(cmd[1], cmd[2]);
        case "jump_to" -> jumpTo(cmd[1]);
        default -> false;
      };
    } catch (Exception e) {
      System.err.println("Invalid debug input.");
      return false;
    }
  }

  private boolean getReg(String regAddress) {
    int reg = Integer.parseInt(regAddress);
    int val = machine.get(reg + Machine.MODULUS);
    System.err.printf("r%d = %d%n", reg, val);
    return true;
  }

  private boolean getMem(String address) {
    int mem = Integer.parseInt(address);
    int val = machine.get(mem);
    System.err.printf("[%d] = %d%n", mem, val);
    return true;
  }

  private boolean setReg(String regAddress, String valString) {
    int reg = Integer.parseInt(regAddress);
    int val = Integer.parseInt(valString);
    machine.put(reg + Machine.MODULUS, val);
    System.err.printf("r%d = %d%n", reg, val);
    return true;
  }

  private boolean setMem(String address, String valString) {
    int mem = Integer.parseInt(address);
    int val = Integer.parseInt(valString);
    machine.put(mem, val);
    System.err.printf("[%d] = %d%n", mem, val);
    return true;
  }

  private boolean jumpTo(String address) throws IOException {
    int index = Integer.parseInt(address);
    machine.setNextInstructionIndex(index);
    System.err.println("jumping to " + index);
    pipedOutput.write('\n'); // advance the machine
    return true;
  }
}
