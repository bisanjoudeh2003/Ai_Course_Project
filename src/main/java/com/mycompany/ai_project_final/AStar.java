package com.mycompany.ai_project_final;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import java.util.*;
import javax.swing.table.DefaultTableModel;



import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AStar {
    private final Cell[][] maze;
    private final Perceptron perceptron;
    private Cell start;
    private Cell end;
    private final List<Cell> openList = new ArrayList<>();
    private final List<Cell> closedList = new ArrayList<>();

    public AStar(Cell[][] maze, Point startPoint, Point endPoint, Perceptron perceptron) {
        this.maze = maze;
        this.perceptron = perceptron;
        this.start = maze[startPoint.x][startPoint.y];
        this.end = maze[endPoint.x][endPoint.y];
        
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start or end point is null");
        }
        
        initialize();
    }

    private void initialize() {
        // Initialize all cells
        for (Cell[] row : maze) {
            for (Cell cell : row) {
                if (cell != null) {
                    cell.gCost = Double.POSITIVE_INFINITY;
                    cell.hCost = 0;
                    cell.fCost = 0;
                    cell.parent = null;
                }
            }
        }

        // Initialize start cell
        start.gCost = 0;
        start.hCost = calculateHeuristic(start);
        start.fCost = start.gCost + start.hCost;
        openList.add(start);
    }

    public void solve() {
        System.out.println("Starting pathfinding from (" + start.row + "," + start.col + 
                         ") to (" + end.row + "," + end.col + ")");
        
        int steps = 0;
        final int maxSteps = maze.length * maze[0].length * 2; // Prevent infinite loops

        while (!openList.isEmpty() && steps++ < maxSteps) {
            Cell current = getNodeWithLowestF();
            
            if (current.equals(end)) {
                reconstructPath(current);
                return;
            }

            openList.remove(current);
            closedList.add(current);

            for (Cell neighbor : getNeighbors(current)) {
                if (closedList.contains(neighbor)) {
                    continue;
                }

                double tentativeG = current.gCost + getMovementCost(current, neighbor);

                if (!openList.contains(neighbor)) {
                    openList.add(neighbor);
                } else if (tentativeG >= neighbor.gCost) {
                    continue;
                }

                neighbor.parent = current;
                neighbor.gCost = tentativeG;
                neighbor.hCost = calculateHeuristic(neighbor);
                neighbor.fCost = neighbor.gCost + neighbor.hCost;
            }
        }
        System.out.println("No path found after " + steps + " steps.");
       // MazeApp.repaint(); 

    }

    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right

        for (int[] dir : directions) {
            int newRow = cell.row + dir[0];
            int newCol = cell.col + dir[1];

            if (newRow >= 0 && newRow < maze.length && 
                newCol >= 0 && newCol < maze[0].length) {
                
                Cell neighbor = maze[newRow][newCol];
                if (neighbor != null && isSafeTile(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    private boolean isSafeTile(Cell cell) {
        // Start and end points are always safe
        if (cell.isStart || cell.isEnd) {
            return true;
        }
        
        // Convert cell features to integers for Perceptron
        int terrain = getTerrainCode(cell.type);
           int elevation = cell.h;
        
        
       // int elevation = cell.type.equals("grass") ? new Random().nextInt(10) : 0;
        int obstacleDistance = calculateManhattanDistanceToObstacle(cell);
        
        return perceptron.classify(terrain, elevation, obstacleDistance) == 1;
    }

    private int getTerrainCode(String terrainType) {
        return switch (terrainType) {
            case "grass" -> 0;
            case "water" -> 1;
            case "obstacle" -> 2;
            default -> 0;
        };
    }

    private int calculateManhattanDistanceToObstacle(Cell cell) {
        int minDistance = Integer.MAX_VALUE;
        
        for (Cell[] row : maze) {
            for (Cell other : row) {
                if (other != null && other.type.equals("obstacle")) {
                    int distance = Math.abs(cell.row - other.row) + Math.abs(cell.col - other.col);
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }
        }
        
        return minDistance == Integer.MAX_VALUE ? 10 : minDistance; // Default distance if no obstacles
    }

    private double getMovementCost(Cell from, Cell to) {
        // Different terrain types can have different movement costs
        if (to.type.equals("water")) return 2.0; // Higher cost for water
        if (to.type.equals("grass")) return 1.0; // Normal cost for grass
        return 1.5; // Default cost
    }

    private Cell getNodeWithLowestF() {
        Cell best = openList.get(0);
        for (Cell cell : openList) {
            if (cell.fCost < best.fCost || 
               (cell.fCost == best.fCost && cell.hCost < best.hCost)) {
                best = cell;
            }
        }
        return best;
    }

    private double calculateHeuristic(Cell cell) {
        // Manhattan distance
        return Math.abs(cell.row - end.row) + Math.abs(cell.col - end.col);
    }

   private void reconstructPath(Cell goal) {
    List<Cell> path = new ArrayList<>();
    Cell current = goal;

    while (current != null) {
        current.isPath = true; 
        path.add(current);
        current = current.parent;
    }

    Collections.reverse(path);

    System.out.println("Path found (" + path.size() + " steps):");
    for (Cell cell : path) {
        System.out.println("(" + cell.row + ", " + cell.col + ")");
    }

        
        // You could also visualize this path in your MazePanel
    }
}
//**********************************************************************************************

//public class AStar {
//    private final Cell[][] maze;
//    private Cell start;
//    private Cell end;
//    private final List<Cell> openList = new ArrayList<>();
//    private final List<Cell> closedList = new ArrayList<>();
//
//    public AStar(Cell[][] maze, Point startPoint, Point endPoint) {
//        this.maze = maze;
//        this.start = maze[startPoint.x][startPoint.y];
//        this.end = maze[endPoint.x][endPoint.y];
//
//        // Ensure start and end are marked safe
//        start.isSafe = true;
//        end.isSafe = true;
//        
//        
//          // Force start and end to be marked safe
//    start.isSafe = true;
//    end.isSafe = true;
//
//    System.out.println("Start: (" + start.row + ", " + start.col + "), isSafe=" + start.isSafe);
//    System.out.println("End: (" + end.row + ", " + end.col + "), isSafe=" + end.isSafe);
//
//        // Initialize costs
//        for (int row = 0; row < maze.length; row++) {
//            for (int col = 0; col < maze[0].length; col++) {
//                maze[row][col].gCost = Double.POSITIVE_INFINITY;
//                maze[row][col].fCost = Double.POSITIVE_INFINITY;
//                maze[row][col].parent = null;
//            }
//        }
//
//        start.gCost = 0;
//        start.hCost = calculateHeuristic(start);
//        start.fCost = start.gCost + start.hCost;
//        openList.add(start);
//    }
//
//    public void solve() {
//        while (!openList.isEmpty()) {
//            Cell current = getNodeWithLowestFCost();
//
//            if (current == end) {
//                reconstructPath();
//                return;
//            }
//
//            openList.remove(current);
//            closedList.add(current);
//
//            for (Cell neighbor : getNeighbors(current)) {
//                if (closedList.contains(neighbor)) continue;
//
//                double tentativeG = current.gCost + 1;
//
//                if (tentativeG < neighbor.gCost) {
//                    neighbor.parent = current;
//                    neighbor.gCost = tentativeG;
//                    neighbor.hCost = calculateHeuristic(neighbor);
//                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
//
//                    if (!openList.contains(neighbor)) {
//                        openList.add(neighbor);
//                    }
//                }
//            }
//        }
//
//        System.out.println("No path found.");
//    }
//
//    private Cell getNodeWithLowestFCost() {
//        Cell best = openList.get(0);
//        for (Cell c : openList) {
//            if (c.fCost < best.fCost || (c.fCost == best.fCost && c.hCost < best.hCost)) {
//                best = c;
//            }
//        }
//        return best;
//    }
//
//    private List<Cell> getNeighbors(Cell cell) {
//        List<Cell> neighbors = new ArrayList<>();
//        int row = cell.row;
//        int col = cell.col;
//
//        if (row > 0 && maze[row - 1][col].isSafe) neighbors.add(maze[row - 1][col]); // Up
//        if (row < maze.length - 1 && maze[row + 1][col].isSafe) neighbors.add(maze[row + 1][col]); // Down
//        if (col > 0 && maze[row][col - 1].isSafe) neighbors.add(maze[row][col - 1]); // Left
//        if (col < maze[0].length - 1 && maze[row][col + 1].isSafe) neighbors.add(maze[row][col + 1]); // Right
//
//        return neighbors;
//    }
//
//
//    private double calculateHeuristic(Cell cell) {
//        return Math.abs(cell.row - end.row) + Math.abs(cell.col - end.col); // Manhattan distance
//    }
//
//    private void reconstructPath() {
//        List<Cell> path = new ArrayList<>();
//        Cell current = end;
//
//        while (current != null) {
//            path.add(current);
//            current = current.parent;
//        }
//
//        Collections.reverse(path);
//
//        // Validate path is continuous
//        boolean valid = true;
//        for (int i = 1; i < path.size(); i++) {
//            Cell prev = path.get(i - 1);
//            Cell curr = path.get(i);
//            int dr = Math.abs(prev.row - curr.row);
//            int dc = Math.abs(prev.col - curr.col);
//            if (dr + dc != 1) {
//                valid = false;
//                break;
//            }
//        }
//
//        if (!valid || path.get(0) != start || path.get(path.size() - 1) != end) {
//            System.out.println("No path found.");
//            return;
//        }
//
//        System.out.println("Safe path from start to end (indices in closed list):");
//        for (Cell cell : path) {
//            int index = closedList.indexOf(cell);
//            System.out.println("Cell at (" + cell.row + ", " + cell.col + ") - Closed list index: " + index);
//        }
//    }
//}








//************************************************************************************************
//import java.util.*;
//
//public class AStarPathfinder {
//
//    public static List<Node> findPath(int[][] grid, int[] start, int[] end) {
//        int rows = grid.length, cols = grid[0].length;
//        Node startNode = new Node(start[0], start[1], 0, 0, null);
//        Node endNode = new Node(end[0], end[1], 0, 0, null);
//
//        PriorityQueue<Node> openSet = new PriorityQueue<>();
//        Set<Cell> closedSet = new HashSet<>();
//
//        openSet.add(startNode);
//
//        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
//
//        while (!openSet.isEmpty()) {
//            Cell current = openSet.poll();
//
//            if (current.row == endNode.row && current.col == endNode.col) {
//                return reconstructPath(current);
//            }
//
//            closedSet.add(current);
//
//            for (int[] dir : directions) {
//                int nx = current.x + dir[0];
//                int ny = current.y + dir[1];
//
//                if (nx < 0 || ny < 0 || nx >= rows || ny >= cols || grid[nx][ny] != 0)
//                    continue;
//
//                Cell neighbor = new Cell(nx, ny, current.g + 1, 0, current);
//                if (closedSet.contains(neighbor))
//                    continue;
//
//                neighbor.h = heuristic(neighbor, endNode);
//
//                boolean skip = false;
//                for (Cell node : openSet) {
//                    if (node.equals(neighbor) && node.f() <= neighbor.f()) {
//                        skip = true;
//                        break;
//                    }
//                }
//
//                if (!skip) {
//                    openSet.add(neighbor);
//                }
//            }
//        }
//
//        return null; // No path found
//    }
//
//    private static int heuristic(Cell a, Cell b) {
//        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col); // Manhattan distance
//    }
//
//    private static List<Cell> reconstructPath(Cell node) {
//        List<Cell> path = new ArrayList<>();
//        while (node != null) {
//            path.add(node);
//            node = node.parent;
//        }
//        Collections.reverse(path);
//        return path;
//    }
//}
