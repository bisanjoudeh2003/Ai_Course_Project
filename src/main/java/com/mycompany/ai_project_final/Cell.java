/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ai_project_final;

/**
 *
 * @author kharo
 */

    class Cell {
        int row , col;
        String type = ""; 
        boolean isStart = false, isEnd = false;
        int obstacleDistance; 
        public boolean isSafe;
        
        public double gCost = 0;
        public double hCost = 0;
        public double fCost = 0;
        public Cell parent = null;
        int h=0;
        public boolean isPath = false;// by bisan 



        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
         // Setter methods to mark start and end points
    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
    }

