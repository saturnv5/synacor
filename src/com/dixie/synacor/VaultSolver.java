package com.dixie.synacor;

import com.google.common.base.Predicates;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class VaultSolver {
  private static final String VAULT_GRID_PATH = "./res/vault_grid.txt";
  private static final int START_WEIGHT = 22;
  private static final int END_WEIGHT = 30;
  private static final Point START = new Point(0, 3);
  private static final Point END = new Point(3, 0);

  public static void main(String[] args) throws IOException {
    Node solution = new VaultSolver(VAULT_GRID_PATH).search(START_WEIGHT, END_WEIGHT, START, END);
    if (solution == null) {
      System.out.println("No solution found.");
    } else {
      System.out.println("Solution: " + solution.directions());
    }
  }

  private final String[][] grid = new String[4][4];

  private enum Direction {
    NORTH(0, -1),
    SOUTH(0, 1),
    EAST(1, 0),
    WEST(-1, 0);

    final int x, y;

    Direction(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  VaultSolver(String path) throws IOException {
    BufferedReader reader = Files.newBufferedReader(Path.of(path));
    for (int y = 0; y < 4; y++) {
      String[] line = reader.readLine().split(" ");
      for (int x = 0; x < 4; x++) {
        grid[x][y] = line[x];
      }
    }
  }

  Node search(int startOrbWeight, int endOrbWeight, Point start, Point end) {
    ArrayDeque<Node> queue = new ArrayDeque<>();
    queue.offer(new Node(start, startOrbWeight));

    while (!queue.isEmpty()) {
      Node cur = queue.poll();
      if (cur.from != null && cur.location.equals(start)) {
        continue;
      }
      if (cur.location.equals(end)) {
        if (cur.orbWeight == endOrbWeight) {
          return cur;
        }
        continue;
      }
      cur.neighbours().forEach(queue::offer);
    }

    return  null;
  }

  private class Node {
    Point location;
    int orbWeight;
    Direction fromDirection;
    Node from;

    Node(Point location, int orbWeight) {
      this.location = location;
      this.orbWeight = orbWeight;
    }

    Stream<Node> neighbours() {
      return Arrays.stream(Direction.values()).map(this::move).filter(Predicates.notNull());
    }

    private Node move(Direction direction) {
      Point newLocation = new Point(location.x + direction.x, location.y + direction.y);
      if (newLocation.x < 0 || newLocation.x >= 4 || newLocation.y < 0 || newLocation.y >= 4) {
        return null;
      }
      String op = grid[location.x][location.y];
      String newOp = grid[newLocation.x][newLocation.y];
      int newOrbWeight = orbWeight;
      switch (op) {
        case "+" -> newOrbWeight += Integer.parseInt(newOp);
        case "-" -> newOrbWeight -= Integer.parseInt(newOp);
        case "*" -> newOrbWeight *= Integer.parseInt(newOp);
      }
      newOrbWeight %= Machine.MODULUS;
      Node newNode = new Node(newLocation, newOrbWeight);
      newNode.fromDirection = direction;
      newNode.from = this;
      return newNode;
    }

    List<Direction> directions() {
      ArrayList<Direction> directions = new ArrayList<>();
      Node n = this;
      while (n != null && n.fromDirection != null) {
        directions.add(n.fromDirection);
        n = n.from;
      }
      Collections.reverse(directions);
      return directions;
    }
  }
}
