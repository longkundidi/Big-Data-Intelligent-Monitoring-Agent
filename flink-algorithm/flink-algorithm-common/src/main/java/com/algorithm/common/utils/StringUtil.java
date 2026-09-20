package com.algorithm.common.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static boolean isEmpty(CharSequence cs) {
        return StringUtils.isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        return StringUtils.isBlank(cs);
    }

}
