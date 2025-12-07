/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ai_project_final;

import java.util.Random;

/**
 *
 * @author Bisan M Joudeh
 */
public class Perceptron {
    private double[] weights;
    private double alpha = 0.1; 
    private double threshold =  0.5; //-0.5
    private static final int properties = 3; 

   
    public Perceptron() {
        weights = new double[properties];
        Random random = new Random(); 
        //   Random random = new Random(42); 
        for (int i = 0; i < properties; i++) {
            weights[i] = random.nextDouble() - 0.5; 
        }
       
    }

 
    private double activation(double BigX) {
    return BigX >= 0 ? 1 : 0; // Step Function
}


    public void train(int[][] trainingData, int maxEpochs) {
        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            boolean allCorrect = true;
            int correctPredictions = 0; 

            for (int[] data : trainingData) {
                int terrain = data[0];
                int elevation = data[1];
                int obstacleDistance = data[2];
                int label = data[3];

                double BigX = terrain * weights[0] + elevation * weights[1] + obstacleDistance * weights[2] - threshold;
                double prediction = activation(BigX);
                int predictedLabel = (int) prediction;
                int error = label - predictedLabel;

                if (error != 0) {
                    allCorrect = false;
                  
                    weights[0] += alpha * error * terrain;
                    weights[1] += alpha * error * elevation;
                    weights[2] += alpha * error * obstacleDistance;
                    threshold -= alpha * error;
                } else {
                    correctPredictions++;
                }
            }

           // double accuracy = (correctPredictions / (double) trainingData.length) * 100; 
         //   System.out.println("Epoch " + (epoch + 1) + " completed. Accuracy: " + accuracy + "%");

           // if (allCorrect) {
            //    System.out.println("Perceptron has completed " + (epoch + 1) + " epochs");
             //   break;
          //  }
        }
    }

    public int classify(int terrain, int elevation, int obstacleDistance) {
        double BigX = terrain * weights[0] + elevation * weights[1] + obstacleDistance * weights[2] - threshold;
        double prediction = activation(BigX);
     return (int) prediction;

    }
      public double evaluateAccuracy(int[][] testData) {
        int correct = 0;
        for (int[] data : testData) {
            int terrain = data[0];
            int elevation = data[1];
            int obstacleDistance = data[2];
            int label = data[3];

            int predictedLabel = classify(terrain, elevation, obstacleDistance);
            if (predictedLabel == label) {
                correct++;
            }
        }
        return (correct / (double) testData.length) * 100.0;
    }
}

