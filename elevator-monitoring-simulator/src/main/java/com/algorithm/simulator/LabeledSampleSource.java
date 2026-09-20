package com.algorithm.simulator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

final class LabeledSampleSource {

    static final int WINDOW_SIZE = 1024;

    static final int SAMPLES_PER_CLASS = 300;

    static final int NORMALS_PER_FAULT = 30;

    static final int SEQUENCE_LENGTH = NORMALS_PER_FAULT + 1;

    private static final int CLASS_COUNT = 8;

    private static final String RESOURCE = "/datasets/elevator-45hz.bin.gz";

    private static final byte[] MAGIC = new byte[] { 'E', 'L', 'E', 'V', '4', '5', 'V', '1' };

    private static final String[] CLASS_CODES = {
            "Normal", "Half", "All", "Oil", "Carbon", "Lowforce", "Gap", "Distance"
    };

    private static final String[] CLASS_NAMES = {
            "正常",
            "闸瓦表面部分磨损",
            "闸瓦表面全磨损",
            "闸瓦接触面存在油污",
            "闸瓦接触面存在异物",
            "弹簧提供的制动力不足",
            "闸瓦和制动轮间隙过大",
            "闸瓦和制动轮未紧密贴合"
    };

    private final float[][][] samples;

    private final Random random;

    private final ShuffledDeck[] sampleDecks;

    private final ShuffledDeck faultClassDeck;

    private long sequenceNumber;

    private int sequencePosition;

    LabeledSampleSource(long randomSeed) throws IOException {
        this.samples = loadSamples();
        this.random = new Random(randomSeed);
        this.sampleDecks = new ShuffledDeck[CLASS_COUNT];
        for (int label = 0; label < CLASS_COUNT; label++) {
            sampleDecks[label] = new ShuffledDeck(SAMPLES_PER_CLASS, 0, random);
        }
        this.faultClassDeck = new ShuffledDeck(CLASS_COUNT - 1, 1, random);
    }

    synchronized SampleWindow nextWindow() {
        int label = sequencePosition < NORMALS_PER_FAULT ? 0 : faultClassDeck.next();
        int sampleIndex = sampleDecks[label].next();
        double[] values = new double[WINDOW_SIZE];
        for (int index = 0; index < WINDOW_SIZE; index++) {
            values[index] = samples[label][sampleIndex][index];
        }

        sequenceNumber++;
        int currentPosition = sequencePosition + 1;
        sequencePosition = (sequencePosition + 1) % SEQUENCE_LENGTH;
        return new SampleWindow(values, label, CLASS_CODES[label], CLASS_NAMES[label], sampleIndex,
                sequenceNumber, currentPosition);
    }

    String description() {
        return "embedded-45hz:8x300x1024";
    }

    static String className(int label) {
        if (label < 0 || label >= CLASS_NAMES.length) {
            throw new IllegalArgumentException("Unknown label: " + label);
        }
        return CLASS_NAMES[label];
    }

    private static float[][][] loadSamples() throws IOException {
        InputStream resource = LabeledSampleSource.class.getResourceAsStream(RESOURCE);
        if (resource == null) {
            throw new IOException("Embedded dataset is missing: " + RESOURCE);
        }
        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(new GZIPInputStream(resource, 64 * 1024)))) {
            for (byte expected : MAGIC) {
                if (input.readByte() != expected) {
                    throw new IOException("Embedded dataset has an invalid header");
                }
            }
            int classCount = input.readInt();
            int sampleCount = input.readInt();
            int windowSize = input.readInt();
            if (classCount != CLASS_COUNT || sampleCount != SAMPLES_PER_CLASS || windowSize != WINDOW_SIZE) {
                throw new IOException("Unexpected embedded dataset dimensions: " + classCount + "x"
                        + sampleCount + "x" + windowSize);
            }

            float[][][] result = new float[CLASS_COUNT][SAMPLES_PER_CLASS][WINDOW_SIZE];
            for (int expectedLabel = 0; expectedLabel < CLASS_COUNT; expectedLabel++) {
                int label = input.readInt();
                if (label != expectedLabel) {
                    throw new IOException("Unexpected class label " + label + "; expected " + expectedLabel);
                }
                for (int sample = 0; sample < SAMPLES_PER_CLASS; sample++) {
                    for (int value = 0; value < WINDOW_SIZE; value++) {
                        float current = input.readFloat();
                        if (!Float.isFinite(current)) {
                            throw new IOException("Embedded dataset contains NaN or infinity");
                        }
                        result[label][sample][value] = current;
                    }
                }
            }
            try {
                input.readByte();
                throw new IOException("Embedded dataset contains trailing bytes");
            }
            catch (EOFException expected) {
                return result;
            }
        }
    }

    private static final class ShuffledDeck {

        private final List<Integer> values;

        private final Random random;

        private int position;

        private ShuffledDeck(int size, int firstValue, Random random) {
            this.values = new ArrayList<>(size);
            this.random = random;
            for (int index = 0; index < size; index++) {
                values.add(firstValue + index);
            }
            shuffle();
        }

        private int next() {
            if (position >= values.size()) {
                shuffle();
            }
            return values.get(position++);
        }

        private void shuffle() {
            Collections.shuffle(values, random);
            position = 0;
        }

    }

}
