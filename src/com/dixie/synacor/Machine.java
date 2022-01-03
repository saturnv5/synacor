package com.dixie.synacor;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;

public class Machine {
  public static final int MODULUS = Short.MAX_VALUE + 1;

  private static final boolean R7_LOCATOR_MODE = false;

  private final HashMap<Integer, Integer> memory = new HashMap<>();
  private final ArrayDeque<Integer> stack = new ArrayDeque<>();
  private final int[] registers = new int[8];
  private final HashSet<Integer> skips = new HashSet<>();

  private final InputStream in;
  private final PrintStream out;

  private int index = 0;
  private boolean skipMode = false;

  public Machine(InputStream in, PrintStream out, byte[] instructions) {
    this(in, out, instructionsFromBytes(instructions));
  }

  public Machine(InputStream in, PrintStream out, int[] instructions) {
    this.in = in;
    this.out = out;
    IntStream.range(0, instructions.length).forEach(i -> memory.put(i, instructions[i]));
  }

  public void execute() throws IOException {
    while (executeNext()) {
      skipMode = skips.contains(index);
    }
  }

  private boolean executeNext() throws IOException {
    int op = next();
    switch (op) {
      case 0 -> { return false; } // halt
      case 1 -> set();
      case 2 -> push();
      case 3 -> pop();
      case 4 -> eq();
      case 5 -> gt();
      case 6 -> jmp();
      case 7 -> jt();
      case 8 -> jf();
      case 9 -> add();
      case 10 -> mult();
      case 11 -> mod();
      case 12 -> and();
      case 13 -> or();
      case 14 -> not();
      case 15 -> rmem();
      case 16 -> wmem();
      case 17 -> call();
      case 18 -> { return ret(); }
      case 19 -> out();
      case 20 -> in();
      case 21 -> { return true; } // no-op
      default -> throw new RuntimeException("Op not implemented: " + op);
    }
    return true;
  }

  private void set() {
    put(next(), nextArg());
  }

  private void push() {
    int val = nextArg();
    if (skipMode) {
      return;
    }
    stack.push(val);
  }

  private void pop() {
    int address = next();
    if (skipMode) {
      return;
    }
    put(address, stack.pop());
  }

  private void eq() {
    put(next(), nextArg() == nextArg());
  }

  private void gt() {
    put(next(), nextArg() > nextArg());
  }

  private void jmp() {
    jumpTo(nextArg());
  }

  private void jt() {
    int val = nextArg();
    int jumpTo = nextArg();
    if (val != 0) {
      jumpTo(jumpTo);
    }
  }

  private void jf() {
    int val = nextArg();
    int jumpTo = nextArg();
    if (val == 0) {
      jumpTo(jumpTo);
    }
  }

  private void add() {
    put(next(), (nextArg() + nextArg()) % MODULUS);
  }

  private void mult() {
    put(next(), (nextArg() * nextArg()) % MODULUS);
  }

  private void mod() {
    put(next(), nextArg() % nextArg());
  }

  private void and() {
    put(next(), nextArg() & nextArg());
  }

  private void or() {
    put(next(), nextArg() | nextArg());
  }

  private void not() {
    put(next(), ~nextArg() & 0x7FFF);
  }

  private void rmem() {
    put(next(), memory.get(nextArg()));
  }

  private void wmem() {
    put(nextArg(), nextArg());
  }

  private void call() {
    int jumpTo = nextArg();
    if (skipMode) {
      return;
    }
    stack.push(index);
    jumpTo(jumpTo);
  }

  private boolean ret() {
    if (skipMode) {
      return true;
    }
    if (stack.isEmpty()) {
      return false;
    }
    jumpTo(stack.pop());
    return true;
  }

  private void out() {
    out.print((char) nextArg());
  }

  private void in() throws IOException {
    put(next(), in.read());
  }

  private int next() {
    return memory.get(index++);
  }

  private int nextArg() {
    int val = next();
    if (val < MODULUS) {
      return val;
    } else {
      if (R7_LOCATOR_MODE) {
        if (val % MODULUS == 7) {
          System.err.println("r7 read near: " + index);
        }
      }
      return registers[val % MODULUS];
    }
  }

  private void put(int address, boolean val) {
    put(address, val ? 1 : 0);
  }

  void put(int address, int val) {
    if (skipMode) {
      return;
    }
    if (address < MODULUS) {
      memory.put(address, val);
    } else {
      registers[address % MODULUS] = val;
    }
  }

  int get(int address) {
    if (address < MODULUS) {
      return memory.get(address);
    } else {
      return registers[address % MODULUS];
    }
  }

  void skipInstruction(int index) {
    skips.add(index);
  }

  private void jumpTo(int index) {
    if (skipMode) {
      return;
    }
    this.index = index;
  }

  public static int[] instructionsFromBytes(byte[] bytes) {
    int[] instructions = new int[bytes.length / 2];
    byte empty = (byte) 0;
    IntStream.range(0, instructions.length).forEach(
            i -> instructions[i] = Ints.fromBytes(empty, empty, bytes[i * 2 + 1], bytes[i * 2]));
    return instructions;
  }
}
