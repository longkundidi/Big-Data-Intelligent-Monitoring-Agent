package com.algorithm.common.utils;

public class Tools {

    public static Float mockFloatBetween(Float begin, Float end) {
        return (float) Math.random() * (end - begin) + begin;
    }

    public static Double mockDoubleBetween(Double begin, Double end) {
        return Math.random() * (end - begin) + begin;
    }

}
