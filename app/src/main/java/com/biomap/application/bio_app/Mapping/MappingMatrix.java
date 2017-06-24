package com.biomap.application.bio_app.Mapping;

/**
 * Creates an object to run calculations on the MappingActivity grid. The input comes straight from
 * the pressure-reading device.
 * <p>
 * Created by Hamish Brindle on 2017-06-22.
 */

public class MappingMatrix {

    private int[] input;
    private int[][] input2D;
    private int startResolution;
    private int endResolution;
    private boolean is2D;

    MappingMatrix(int input[], int startResolution, int endResolution) {
        this.input = input;
        this.startResolution = startResolution;
        this.endResolution = endResolution;
        is2D = false;
    }

    public MappingMatrix(int input[][], int startResolution, int endResolution) {
        this.input2D = input;
        this.startResolution = startResolution;
        this.endResolution = endResolution;
        is2D = true;
    }

    int[][] convertMatrix2D(int[] input) {
        int[][] output = new int[startResolution][startResolution];
        int count = 0;

        for (int yPos = 0; yPos < startResolution; yPos++) {
            for (int xPos = 0; xPos < startResolution; xPos++) {
                output[xPos][yPos] = input[count++];
            }
        }
        return output;
    }

    public int[][] expandMatrix(int[][] input2D) {

        int[][] newOutput;

        newOutput = stretchMatrixHorizontal(input2D, startResolution, endResolution);
        //newOutput = stretchMatrixHorizontal(newOutput, newOutput.length, newOutput[0].length);
        newOutput = rotateMatrix90(newOutput);
        newOutput = stretchMatrixHorizontal(newOutput, endResolution, endResolution);
        newOutput = rotateMatrix90(newOutput);
        newOutput = rotateMatrix90(newOutput);
        newOutput = rotateMatrix90(newOutput);

        return newOutput;
    }

    private int[][] stretchMatrixHorizontal(int[][] input, int height, int width) {

        int[][] output = new int[height][width];
        int index = 0;
        int padding;

        // Take the input array (2D) and expand it from starting resolution to end resolution.
        for (int yPos = 0; yPos < height; yPos++) {
            for (int xPos = 0; xPos < height; xPos++) {
                if (index >= width - 1)
                    break;
                if (xPos == 0) {
                    padding = (int) (input[yPos][xPos] / 1.15);
                    output[yPos][index++] = padding;
                    output[yPos][index++] = input[yPos][xPos];
                } else if (xPos == (height - 1)) {
                    padding = (input[yPos][xPos - 1] + input[yPos][xPos]) / 2;
                    output[yPos][index++] = padding;
                    padding = (int) (input[yPos][xPos] / 1.15);
                    output[yPos][index++] = input[yPos][xPos];
                    output[yPos][index++] = padding;
                } else {
                    padding = (input[yPos][xPos - 1] + input[yPos][xPos]) / 2;
                    output[yPos][index++] = padding;
                    output[yPos][index++] = input[yPos][xPos];
                }

            }

            index = 0;
        }
        return output;
    }

    private int[][] rotateMatrix90(int[][] input) {

        int[][] output = new int[input[0].length][input.length];

        int yLength = input.length ;
        int xLength = input[0].length;

        for (int i = 0; i < yLength; i++) {
            for (int j = 0; j < xLength; j++) {
                output[j][i] = input[i][j];
            }
        }

        for (int i = 0, k = output.length - 1; i < k; ++i, --k) {
            int[] x = output[i];
            output[i] = output[k];
            output[k] = x;
        }

        return output;
    }


}
