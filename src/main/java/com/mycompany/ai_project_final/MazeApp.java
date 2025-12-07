/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ai_project_final;

/**
 *
 * @author Bisan M Joudeh
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


import javax.swing.JFrame;

import java.util.*;
import javax.swing.table.DefaultTableModel;



public class MazeApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeFrame::new);
    }
}


class MazeFrame extends JFrame {
    private final int CELL_SIZE = 40;
    private Cell[][] maze;
    private int rows, cols;
    private Point startPoint = null;
    private Point endPoint = null;
    private MazePanel mazePanel;
    private Perceptron perceptron;
    private DefaultTableModel tableModel;
    private List<Cell> safeCells = new ArrayList<>(); // to use it when apply A*
        

    public MazeFrame() {
        setTitle("Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize Perceptron
        perceptron = new Perceptron();
        perceptron.train(TrainingData.trainingData, 1000); 
        double testAccuracy = perceptron.evaluateAccuracy(testData.testData1);
         System.out.println("Test accuracy: " + testAccuracy + "%"); // new

       
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JLabel rowLabel = new JLabel("Rows:");
        JLabel colLabel = new JLabel("Cols:");
        JTextField rowField = new JTextField(3);
        JTextField colField = new JTextField(3);

        rowLabel.setFont(new Font("Arial", Font.BOLD, 14));
        colLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rowField.setFont(new Font("Arial", Font.PLAIN, 14));
        colField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton createButton = new JButton("Create Maze");
        JButton solveButton = new JButton("Solve");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        solveButton.setFont(new Font("Arial", Font.BOLD, 14));

        topPanel.add(rowLabel);
        topPanel.add(rowField);
        topPanel.add(colLabel);
        topPanel.add(colField);
        topPanel.add(createButton);
        topPanel.add(solveButton);
        add(topPanel, BorderLayout.NORTH);

        //table to display cell 
        String[] columns = {"Row", "Column", "Terrain", "Elevation", "Obstacle Distance", "Predicted Label"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(400, 300));
        add(tableScrollPane, BorderLayout.EAST);

       
        mazePanel = new MazePanel();
        JScrollPane scrollPane = new JScrollPane(mazePanel);
        add(scrollPane, BorderLayout.CENTER);

        setSize(1200, 700);
        setLocationRelativeTo(null);
        setVisible(true);

      
        createButton.addActionListener(e -> {
            try {
                rows = Integer.parseInt(rowField.getText());
                cols = Integer.parseInt(colField.getText());
                maze = new Cell[rows][cols];
                for (int i = 0; i < rows; i++)
                    for (int j = 0; j < cols; j++)
                        maze[i][j] = new Cell(i, j);
                mazePanel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
                mazePanel.revalidate();
                mazePanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
            }
        });


solveButton.addActionListener(e -> {
    if (startPoint == null || endPoint == null) {
        JOptionPane.showMessageDialog(this, "Please set both start and end points");
        return;
    }
    
    classifyCells(); // First classify all cells
    
    try {
        AStar aStar = new AStar(maze, startPoint, endPoint, perceptron);
        aStar.solve();
        
        // Optional: Visualize the path in your maze panel
        mazePanel.repaint();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error during pathfinding: " + ex.getMessage());
    }
});
        
     
    }


  private void classifyCells() {
    tableModel.setRowCount(0);
    safeCells.clear();       

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            Cell cell = maze[i][j];
 int elevation = cell.h;
           // int elevation = (cell.type.equals("grass")) ? new Random().nextInt(10) : 0; // elevation just if it grass
            int obstacleDistance = calculateManhattanDistance(i, j);
            cell.obstacleDistance = obstacleDistance;

            int terrain = switch (cell.type) {
                case "grass" -> 0;
                case "water" -> 1;
                case "obstacle" -> 2;
                default -> 0;
            };

            String labelText;
            boolean isSafe;

            if (cell.isStart || cell.isEnd) {
                labelText = "Safe"; // alawys safe 
                isSafe = true;
            } else {
                int predictedLabel = perceptron.classify(terrain, elevation, obstacleDistance); // هون المكان يلي بنستخدم فيه البيسبترون المدرب للتصنف
                isSafe = predictedLabel == 1;
                labelText = isSafe ? "Safe" : "Not Safe";
            }
            

            if (isSafe) {
                safeCells.add(cell); // add it to safe list 
            }

            tableModel.addRow(new Object[]{
                i, j, cell.type, elevation, obstacleDistance, labelText
            });
        }
    }
}




    private int calculateManhattanDistance(int row, int col) {

    int minDistance = Integer.MAX_VALUE;

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (maze[i][j].type.equals("obstacle")) {
                int distance = Math.abs(row - i) + Math.abs(col - j);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
    }

    return minDistance == Integer.MAX_VALUE ? -1 : minDistance;


    }

  
    class MazePanel extends JPanel {
        public MazePanel() {
            setPreferredSize(new Dimension(800, 500));
            setBackground(Color.WHITE);


   addMouseListener(new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        if (maze == null) return;
        int row = e.getY() / CELL_SIZE;
        int col = e.getX() / CELL_SIZE;
        if (row >= rows || col >= cols) return;

        String[] options = {"Grass", "Water", "Obstacle", "Start", "End"};
        int choice = JOptionPane.showOptionDialog(
                MazeFrame.this, "Select cell type:", "Cell Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == -1) return;

        Cell cell = maze[row][col];
        switch (choice) {
            case 0 -> { // Grass
                cell.type = "grass";
                boolean validInput = false;
                while (!validInput) {
                    String input = JOptionPane.showInputDialog(
                            MazeFrame.this,
                            "Enter height for grass cell (1-10):",
                            "Grass Height",
                            JOptionPane.PLAIN_MESSAGE);
                    if (input == null) break; // المستخدم ضغط Cancel
                    try {
                        int height = Integer.parseInt(input);
                        if (height >= 1 && height <= 10) {
                            cell.h = height;
                            validInput = true;
                        } else {
                            JOptionPane.showMessageDialog(
                                    MazeFrame.this,
                                    "Height must be between 1 and 10.",
                                    "Invalid Input",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                                MazeFrame.this,
                                "Please enter a valid integer.",
                                "Invalid Input",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
            case 1 -> cell.type = "water";
            case 2 -> cell.type = "obstacle";
            case 3 -> { // Start point
                if (startPoint != null)
                    maze[startPoint.x][startPoint.y].isStart = false;
                startPoint = new Point(row, col);
                cell.isStart = true;
                cell.isEnd = false;
            }
            case 4 -> { // End point
                if (endPoint != null)
                    maze[endPoint.x][endPoint.y].isEnd = false;
                endPoint = new Point(row, col);
                cell.isEnd = true;
                cell.isStart = false;
            }
        }
        repaint();
    }
});
        }
        
        

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (maze == null) return;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Cell cell = maze[i][j];
                    
                      if (cell.isPath) {
                g.setColor(new Color(128, 0, 128));
                      }    // Purple for solution path
               else if (cell.isStart) g.setColor(Color.ORANGE);
                    else if (cell.isEnd) g.setColor(Color.RED);
                    else {
                        switch (cell.type) {
                            case "grass" -> g.setColor(new Color(102, 204, 0));
                            case "water" -> g.setColor(new Color(102, 204, 255));
                            case "obstacle" -> g.setColor(Color.BLACK);
                            default -> g.setColor(Color.LIGHT_GRAY);
                        }
                    }
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(j * 
                            
                            CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
            
        }
    }

}

     
   