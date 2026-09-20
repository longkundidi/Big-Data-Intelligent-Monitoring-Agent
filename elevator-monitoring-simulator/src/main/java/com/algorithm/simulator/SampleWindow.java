package com.algorithm.simulator;

final class SampleWindow {

    private final double[] values;

    private final int label;

    private final String classCode;

    private final String className;

    private final int sampleIndex;

    private final long sequenceNumber;

    private final int sequencePosition;

    SampleWindow(double[] values, int label, String classCode, String className, int sampleIndex,
            long sequenceNumber, int sequencePosition) {
        this.values = values;
        this.label = label;
        this.classCode = classCode;
        this.className = className;
        this.sampleIndex = sampleIndex;
        this.sequenceNumber = sequenceNumber;
        this.sequencePosition = sequencePosition;
    }

    double[] getValues() {
        return values;
    }

    int getLabel() {
        return label;
    }

    String getClassCode() {
        return classCode;
    }

    String getClassName() {
        return className;
    }

    int getSampleIndex() {
        return sampleIndex;
    }

    long getSequenceNumber() {
        return sequenceNumber;
    }

    int getSequencePosition() {
        return sequencePosition;
    }

    boolean isFault() {
        return label != 0;
    }

}
