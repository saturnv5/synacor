package com.dixie.synacor;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Decompiler {
  public static void main(String[] args) throws IOException {
    PrintStream out = args.length == 0 ?
            System.out : new PrintStream(Files.newOutputStream(Path.of(args[0])));
    new Decompiler(TextAdventure.FILE_PATH, out).decompile();
  }

  private final int[] instructions;
  private final PrintStream out;
  private int index;

  Decompiler(String path, PrintStream out) throws IOException {
    instructions = Machine.instructionsFromBytes(Files.readAllBytes(Paths.get(path)));
    this.out = out;
  }

  void decompile() {
    while (index < instructions.length) {
      printNextInstruction();
    }
  }

  private void printNextInstruction() {
    out.print(index + ": ");
    int op = instructions[index++];
    String instruction = switch (op) {
      case 0 -> "halt";
      case 1 -> "set " + addressString() + argString();
      case 2 -> "push " + argString();
      case 3 -> "pop " + addressString();
      case 4 -> "eq " + addressString() + argString() + argString();
      case 5 -> "gt " + addressString() + argString() + argString();
      case 6 -> "jmp " + argString();
      case 7 -> "jt " + argString() + argString();
      case 8 -> "jf " + argString() + argString();
      case 9 -> "add " + addressString() + argString() + argString();
      case 10 -> "mult " + addressString() + argString() + argString();
      case 11 -> "mod " + addressString() + argString() + argString();
      case 12 -> "and " + addressString() + argString() + argString();
      case 13 -> "or " + addressString() + argString() + argString();
      case 14 -> "not" + addressString() + argString();
      case 15 -> "rmem " + addressString() + addressString();
      case 16 -> "wmem " + "[" + addressString() + "] " + argString();
      case 17 -> "call " + argString();
      case 18 -> "ret";
      case 19 -> "out " + argString();
      case 20 -> "in " + addressString();
      case 21 -> "noop";
      default -> "unknown " + op;
    };
    out.println(instruction);
  }

  private String argString() {
    int arg = instructions[index++];
    if (arg < Machine.MODULUS) {
      return arg + " ";
    } else {
      return "r" + (arg % Machine.MODULUS) + " ";
    }
  }

  private String addressString() {
    int address = instructions[index++];
    if (address < Machine.MODULUS) {
      return "[" + address + "] ";
    } else {
      return "r" + (address % Machine.MODULUS) + " ";
    }
  }
}
