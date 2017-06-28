package com.biomap.application.bio_app.Mapping;

/**
 * Creates an object to run calculations on the MappingActivity grid. The input comes straight from
 * the pressure-reading device.
 * <p>
 * Created by Hamish Brindle on 2017-06-22.
 */
class MappingMatrix {

    /**
     * Convert a 1D matrix into a
     *
     * @param input Array to be converted.
     * @return Converted array.
     */
    int[][] convert2D(int[] input) {
        int resolution = MappingActivity.NODES_RESOLUTION;
        int[][] output = new int[resolution][resolution];
        int count = 0;

        for (int yPos = 0; yPos < resolution; yPos++) {
            for (int xPos = 0; xPos < resolution; xPos++) {
                output[xPos][yPos] = input[count++];
            }
        }
        return output;
    }

    /**
     * This is a ridiculous method. It stretches the matrix horizontally, flips it, stretches it
     * again, then flips it three more times to get it back to proper orientation.
     * <p>
     * TODO: Make a rotation method that goes the other way PROPERLY god dammit.
     *
     * @param input2D Original matrix to be stretched out.
     * @return New matrix.
     */
    int[][] expand(int[][] input2D) {
        int[][] newOutput;

        // Stretch, rotate, stretch, rotate lol
        newOutput = stretchHorizontal(input2D);
        newOutput = rotate90CounterClockwise(newOutput);
        newOutput = stretchHorizontal(newOutput);
        rotate90ClockWise(newOutput);

        return newOutput;
    }

    /**
     * Stretches a matrix along the x-axis by averaging each value with its neighbour and inserting
     * that value as padding between the two; essentially smoothing a grid out to a higher
     * resolution.
     * <p>
     * NOTE: The relationship between the starting resolution and end resolution must be:
     * (resolution * 2) + 1 = endResolution. For example, if there are 8 actual values in the
     * matrix, the end resolution must be 17 (i.e. 8 * 2 + 1 = 17).
     * <p>
     * I would advise against changing anything here.
     *
     * @param input Original matrix of values to be stretched.
     * @return New matrix of stretched values.
     */
    private int[][] stretchHorizontal(int[][] input) {

        /* The height of the new array won't change from the input array, but the width will be double
         * plus 1.
         */
        int height = input.length;
        int width = (input[0].length * 2) + 1;

        int[][] output = new int[height][width];
        int index = 0;

        double startEndPadding = 1.25;
        int padding;
        int startingResolution = MappingActivity.NODES_RESOLUTION;

        /* Take the input array (2D) and expand it from starting resolution to end resolution.
         * Essentially, the original array is working through itself while adding values from the
         * original to the new array. Since the new array is double in size (plus 1), we use
         * a separate index to achieve the higher range.
         */
        for (int yPos = 0; yPos < height; yPos++) {

            // This inner loop ALWAYS loops through input with the range of NODES_RESOLUTION.
            for (int xPos = 0; xPos < startingResolution; xPos++) {

                /* This case handles adding the first column of padding. It divides the first
                 * actual node by startEndPadding to smooth out it's value.
                 */
                if (xPos == 0) {
                    padding = (int) (input[yPos][xPos] / startEndPadding);
                    output[yPos][index++] = padding;
                    output[yPos][index++] = input[yPos][xPos];

                /* This case handles the last column of padding. Only triggers once the input array
                 * reaches it's end. Again, it divides the last node value by startEndPadding to
                 * smooth it out.
                 */
                } else if (xPos == (startingResolution - 1)) {
                    padding = (input[yPos][xPos - 1] + input[yPos][xPos]) / 2;
                    output[yPos][index++] = padding;
                    padding = (int) (input[yPos][xPos] / startEndPadding);
                    output[yPos][index++] = input[yPos][xPos];
                    output[yPos][index++] = padding;

                /* This case handles everything in between; it averages two nodes to put a average
                 * value between them as padding.
                 */
                } else {
                    padding = (input[yPos][xPos - 1] + input[yPos][xPos]) / 2;
                    output[yPos][index++] = padding;
                    output[yPos][index++] = input[yPos][xPos];
                }
            }

            // Reset index back to zero or else.
            index = 0;
        }

        return output;
    }

    /**
     * Rotate matrix 90 degrees counter-clockwise.
     *
     * @param input Matrix to be affected.
     * @return New matrix.
     */
    private int[][] rotate90CounterClockwise(int[][] input) {

        int[][] output = new int[input[0].length][input.length];

        int yLength = input.length;
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

    /**
     * Rotate matrix 90 degrees clockwise.
     *
     * @param matrix Matrix to be affected.
     */
    private int[][] rotate90ClockWise(int[][] matrix) {

        int[][] output = matrix;
        int len = matrix.length;

        if (output.length != output[0].length)//INVALID INPUT
            return null;

        for (int i = 0; i < len / 2; i++) {
            for (int j = 0; j < len; j++) {
                int temp = matrix[i][j];
                output[i][j] = output[len - 1 - i][j];
                output[len - 1 - i][j] = temp;
            }
        }

        for (int i = 0; i < matrix.length; i++) {
            for (int j = i + 1; j < matrix.length; j++) {
                int temp = matrix[i][j];
                output[i][j] = output[j][i];
                output[j][i] = temp;
            }
        }

        return output;
    }

}
