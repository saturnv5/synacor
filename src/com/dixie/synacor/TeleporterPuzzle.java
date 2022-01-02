package com.dixie.synacor;

import java.util.ArrayDeque;

public class TeleporterPuzzle {
  private int r0 = 4, r1 = 1, r2, r3, r4, r5, r6, r7;

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
  private void originalMethodA() {
    if (r0 == 0) {
      r0 = r1 + 1;
    } else if (r1 == 0) {
      r0--;
      r1 = r7;
      originalMethodA();
    } else {
      int tmp = r0;
      r1--;
      originalMethodA();
      r1 = r0;
      r0 = tmp;
      r0--;
      originalMethodA();
    }
  }

  /** Convert tail-recursion to loop; use r0 as input and output value. */
  private int originalMethodB(int x) {
    while (x > 0) {
      if (r1 == 0) {
        r1 = r7;
      } else {
        r1--;
        r1 = originalMethodB(x);
      }
      x--;
    }
    return r1 + 1;
  }

  /** Use r1 as second input value. */
  private int originalMethodC(int x, int y) {
    while (x > 0) {
      if (y == 0) {
        y = r7;
      } else {
        y = originalMethodC(x, y - 1);
      }
      x--;
    }
    return y + 1;
  }
}
