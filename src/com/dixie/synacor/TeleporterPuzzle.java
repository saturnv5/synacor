package com.dixie.synacor;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashMap;

public class TeleporterPuzzle {
  private int r0 = 4, r1 = 1, r2, r3, r4, r5, r6, r7;

  TeleporterPuzzle(int r7) {
    this.r7 = r7;
  }

  /**
   * 5483: set r0 4
   * 5486: set r1 1
   * 5489: call 6027
   * ...
   * 6027: jt r0 6035
   * 6030: add r0 r1 1
   * 6034: ret
   * 6035: jt r1 6048
   * 6038: add r0 r0 32767
   * 6042: set r1 r7
   * 6045: call 6027
   * 6047: ret
   * 6048: push r0
   * 6050: add r1 r1 32767
   * 6054: call 6027
   * 6056: set r1 r0
   * 6059: pop r0
   * 6061: add r0 r0 32767
   * 6065: call 6027
   * 6067: ret
   */
  private void methodA() {
    if (r0 == 0) {
      r0 = r1 + 1;
    } else if (r1 == 0) {
      r0--;
      r1 = r7;
      methodA();
    } else {
      int tmp = r0;
      r1--;
      methodA();
      r1 = r0;
      r0 = tmp;
      r0--;
      methodA();
    }
  }

  /** Convert tail-recursion to loop; use r0 as input and output value. */
  private int methodB(int x) {
    while (x > 0) {
      if (r1 == 0) {
        r1 = r7;
      } else {
        r1--;
        r1 = methodB(x);
      }
      x--;
    }
    return r1 + 1;
  }

  /** Use r1 as second input value. */
  private int methodC(int x, int y) {
    while (x > 0) {
      if (y == 0) {
        y = r7;
      } else {
        y = methodC(x, y - 1);
      }
      x--;
    }
    return y + 1;
  }

  /** Same thing, but based on methodA again as the while-loop is confusing. */
  private int methodD(int x, int y) {
    if (x == 0) {
      return y + 1;
    } else if (y == 0) {
      y = r7;
    } else {
      y = methodD(x, y - 1);
    }
    return methodD(x - 1, y);
  }

  private final HashMap<Point, Integer> cache = new HashMap<>();

  /** Same as D, but with memoization. */
  public int methodE(int x, int y) {
    Point key = new Point(x, y);
    Integer ans = cache.get(key);
    if (ans != null) {
      return ans;
    }
    if (x == 0) {
      ans = (y + 1) % Machine.MODULUS;
      cache.put(key, ans);
      return ans;
    } else if (y == 0) {
      y = r7;
    } else {
      y = methodE(x, y - 1);
    }
    ans = methodE(x - 1, y);
    cache.put(key, ans);
    return ans;
  }

  /** Abandoned attempt; stack overflow. */
  public static void main1(String[] args) {
    // 5483: set r0 4
    // 5486: set r1 1
    // 5489: call 6027
    // 5491: eq r1 r0 6
    for (int r7 = 0; r7 < Machine.MODULUS; r7++) {
      int output = new TeleporterPuzzle(r7).methodE(4, 1);
      System.out.println(String.format("output = %d, when r7 = %d", output, r7));
      if (output == 6) {
        break;
      }
    }
  }

  /** Second abandoned attempt, but got some values.
   * output = 1, when r7 = 0, x = 0, y = 0
   * output = 2, when r7 = 0, x = 0, y = 1
   * output = 1, when r7 = 0, x = 1, y = 0
   * output = 2, when r7 = 0, x = 1, y = 1
   * output = 1, when r7 = 0, x = 2, y = 0
   * output = 2, when r7 = 0, x = 2, y = 1
   * output = 1, when r7 = 0, x = 3, y = 0
   * output = 2, when r7 = 0, x = 3, y = 1
   * output = 1, when r7 = 0, x = 4, y = 0
   * output = 2, when r7 = 0, x = 4, y = 1
   * output = 1, when r7 = 1, x = 0, y = 0
   * output = 2, when r7 = 1, x = 0, y = 1
   * output = 2, when r7 = 1, x = 1, y = 0
   * output = 3, when r7 = 1, x = 1, y = 1
   * output = 3, when r7 = 1, x = 2, y = 0
   * output = 5, when r7 = 1, x = 2, y = 1
   * output = 5, when r7 = 1, x = 3, y = 0
   * output = 13, when r7 = 1, x = 3, y = 1
   * output = 13, when r7 = 1, x = 4, y = 0
   *
   * 'y' doesn't seem to matter.
   */
  public static void main2(String[] args) {
    for (int r7 = 0; r7 < Machine.MODULUS; r7++) {
      for (int x = 0; x <= 4; x++) {
        for (int y = 0; y <= 1; y++) {
          int output = new TeleporterPuzzle(r7).methodE(x, y);
          System.out.println(
                  String.format("output = %d, when r7 = %d, x = %d, y = %d", output, r7, x, y));
        }
      }
    }
  }

  /** Same as E, but with an explicit stack instead of recursion. */
  private int methodF(int x, int y) {
    ArrayDeque<Integer> stack = new ArrayDeque<>();
    stack.push(x);
    while (!stack.isEmpty()) {
      x = stack.pop();
      if (x == 0) { // base-case
        y = (y + 1) % Machine.MODULUS;
      } else if (y == 0) { // tail-recursion
        y = r7;
        stack.push(x - 1);
      } else { // nested-recursion
        y--;
        stack.push(x - 1);
        stack.push(x); // This will naturally set 'y' to the correct value for the line above.
      }
    }
    return y;
  }

  /**
   * A bit better, I now get an answer for r7 = 1 (32765), r7 = 2 (13234), but r3 takes too long.
   */
  public static void main3(String[] args) {
    for (int r7 = 1; r7 < Machine.MODULUS; r7++) {
      for (int x = 0; x <= 4; x++) {
        int output = new TeleporterPuzzle(r7).methodF(x, 1);
        System.out.println(
                String.format("output = %d, when r7 = %d, x = %d, y = 1", output, r7, x));
        if (output == 6) {
          break;
        }
      }
    }
  }
}
