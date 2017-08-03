package com.biomap.application.bio_app.Mapping;

import android.util.Log;

import java.util.Map;
import java.util.UUID;

/**
 * Contains matrix of pressure nodes.
 * <p>
 * Created by Hamish Brindle on 2017-07-26.
 */

public class PressureNodeMatrix {

    private static final String TAG = "PressureNodeMatrix";
    private Map<UUID, byte[]> values;
    private PressureNode[][] matrix = new PressureNode[8][8];

    public PressureNodeMatrix(Map<UUID, byte[]> values) {

        this.values = values;

        createMatrix();

    }

    public void createMatrix() {

        int i;
        int j;

        byte[] bothBytes = new byte[2];

        boolean isFirstByte = false;
        int nodeNumber = 0;

        int totalBytes = 0;

        i = 0;
        for (Map.Entry entry : values.entrySet()) {
            byte[] bytes = ((byte[]) entry.getValue()); // Row
            j = 0;

            for (byte b : bytes) {

                totalBytes++;

                if (isFirstByte) {

                    bothBytes[0] = b;
                    isFirstByte = false;

                    matrix[j][i] = new PressureNode(nodeNumber++, toInt(bothBytes)); // bytes to int;
                    Log.d(TAG, "createMatrix: Node #" + matrix[j][i].getNodeNumber() + " = " + matrix[j][i].getNodeData());

                } else {

                    bothBytes[1] = b;
                    isFirstByte = true;

                }

                if (totalBytes % 2 == 0)
                    j++;
            }
            i++;
        }
    }

    /**
     * Convert a byte array to a hex string. Used for printing hex values from the micro-controller.
     *
     * @param bytes from characteristic
     * @return String of hex values for the characteristic.
     */
    private String byteArrayToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {

            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }

    /**
     * Convert a byte array to a hex string. Used for printing hex values from the micro-controller.
     *
     * @param b from characteristic
     * @return String of hex values for the characteristic.
     */
    private String byteToHex(byte b) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X ", b));
        return sb.toString();
    }

    private int toInt(byte[] b) {
        int x = (0 << 24) | (0 << 16)
                | ((b[1] & 0xFF) << 8) | ((b[0] & 0xFF) << 0);
        return x;
    }

}
