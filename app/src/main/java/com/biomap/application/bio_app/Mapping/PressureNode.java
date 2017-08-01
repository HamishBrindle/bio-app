package com.biomap.application.bio_app.Mapping;

import android.util.Pair;

/**
 * Pressure sensor node. Holds number of the node and it's corresponding value.
 * <p>
 * Created by Hamish Brindle on 2017-07-26.
 */

public class PressureNode<K, V> extends Pair {

    private int nodeNumber;
    private int nodeData;
    private int valueOffset;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public PressureNode(Integer first, Integer second) {
        super(first, second);
        nodeNumber = first;
        nodeData = second;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public int getNodeData() {
        return nodeData;
    }

    public void setNodeData(byte nodeData) {
        this.nodeData = nodeData;
    }

    public int getValueOffset() {
        return valueOffset;
    }

    public void setValueOffset(int valueOffset) {
        this.valueOffset = valueOffset;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
