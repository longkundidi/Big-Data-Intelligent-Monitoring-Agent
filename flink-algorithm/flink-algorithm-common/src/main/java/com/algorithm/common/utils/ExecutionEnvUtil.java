package com.algorithm.common.utils;

import com.algorithm.common.constant.PropertiesConstants;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.IOException;
/*
创建一个 ParameterTool，它从多个来源（属性文件、命令行参数、系统属性）合并配置信息。这样可以在 Flink 作业中方便地管理和读取配置。

使用场景：在实际应用中，可以通过 PARAMETER_TOOL 获取各种配置，动态地调整 Flink 作业的行为。 */
public class ExecutionEnvUtil {

    public static ParameterTool createParameterTool(final String[] args) throws Exception {
        return ParameterTool
                .fromPropertiesFile(ExecutionEnvUtil.class.getResourceAsStream(PropertiesConstants.PROPERTIES_FILE_NAME))
                .mergeWith(ParameterTool.fromArgs(args))
                .mergeWith(ParameterTool.fromSystemProperties());
    }

    public static final ParameterTool PARAMETER_TOOL = createParameterTool();

    private static ParameterTool createParameterTool() {
        try {
            return ParameterTool
                    .fromPropertiesFile(ExecutionEnvUtil.class.getResourceAsStream(PropertiesConstants.PROPERTIES_FILE_NAME))
                    .mergeWith(ParameterTool.fromSystemProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ParameterTool.fromSystemProperties();
    }

}