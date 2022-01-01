package com.dixie.synacor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

public class Machine {
  private static final int MODULUS = Short.MAX_VALUE + 1;

  private final HashMap<Integer, Integer> memory = new HashMap<>();
  private final int[] registers = new int[8];

  private final InputStream in;
  private final PrintStream out;

  private int index = 0;

  public Machine(InputStream in, PrintStream out, byte[] instructions) {
    this(in, out, instructionsFromBytes(instructions));
  }

  public Machine(InputStream in, PrintStream out, int[] instructions) {
    this.in = in;
    this.out = out;
    IntStream.range(0, instructions.length).forEach(i -> memory.put(i, instructions[i]));
  }

  public void execute() {
    while (executeNext());
    out.println("Next op to implement: " + memory.get(index - 1));
  }

  private boolean executeNext() {
    int op = next();
    return switch (op) {
      case 6 -> jump();
      case 19 -> print();
      case 21 -> true; // no-op
      default -> false;
    };
  }

  private boolean jump() {
    index = nextArg();
    return true;
  }

  private boolean print() {
    out.print((char) nextArg());
    return true;
  }

  private int next() {
    return memory.get(index++);
  }

  private int nextArg() {
    int val = next();
    if (val < MODULUS) {
      return val;
    } else {
      return registers[val % MODULUS];
    }
  }

  private static int[] instructionsFromBytes(byte[] bytes) {
    int[] instructions = new int[bytes.length / 2];
    IntStream.range(0, instructions.length).forEach(
            i -> instructions[i] = bytes[i * 2] + 256 * bytes[i * 2 + 1]);
    return instructions;
  }
}
