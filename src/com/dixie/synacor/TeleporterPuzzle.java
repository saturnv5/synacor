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

  /**
   * Increase the JVM stack size to 1G to run.
   *
   * output = 6, when r7 = 25734
   */
  public static void main(String[] args) {
    for (int r7 = 1; r7 < Machine.MODULUS; r7++) {
      int output = new TeleporterPuzzle(r7).methodE(4, 1);
      System.out.println(
              String.format("output = %d, when r7 = %d", output, r7));
      if (output == 6) {
        break;
      }
    }
  }
}
