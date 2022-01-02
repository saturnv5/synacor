package com.dixie.synacor;

import com.google.common.collect.Collections2;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CoinSolver {
  private static final int TARGET = 399;

  private enum Coin {
    RED(2),
    CORRODED(3),
    SHINY(5),
    CONCAVE(7),
    BLUE(9);

    private final int value;

    private Coin(int value) {
      this.value = value;
    }
  }
  public static void main(String[] args) {
    List<Coin> answer = Collections2.permutations(Arrays.asList(Coin.values())).stream()
            .dropWhile(Predicate.not(CoinSolver::isAnswer)).findFirst().get();
    System.out.println(answer);
  }

  private static boolean isAnswer(List<Coin> coins) {
    int val = coins.get(0).value
            + coins.get(1).value
            * pow(coins.get(2).value, 2)
            + pow(coins.get(3).value, 3)
            - coins.get(4).value;
    return val == TARGET;
  }

  private static int pow(int base, int exp) {
    int ans = base;
    while (exp > 1) {
      ans *= base;
      exp--;
    }
    return ans;
  }
}
