package com.algorithm.common.utils;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MqttUtilGRUAE {

    private static final Properties props;

    static {
        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            throw new RuntimeException("MqttUtilGRUAE 无法加载 application.properties", e);
        }
    }

    // MQTT 服务器地址
    private static final String mqttServer = props.getProperty("mqtt.server");
    // 要发送的主题
    private static final String topic = props.getProperty("mqtt.gruae.topic", "dc_algorithm_GRUAE");
    // 要读取的文件
    private static final String fileName = props.getProperty("file.gruae.name");

    public static void writeToMqtt() throws Exception {
        MqttClient client = initMqttClient();
        Resource resource = new ClassPathResource(fileName);

        while (true) { // 无限循环
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                    Stream<String> lines = reader.lines()
            ) {
                AtomicInteger i = new AtomicInteger();
                lines.forEach(ele -> {
                    try {
                        String cleanedData = ele.replace("\uFEFF", "");
                        long currentTimeMillis = System.currentTimeMillis();
                        String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\": 190}", cleanedData, currentTimeMillis);

                        MqttMessage message = new MqttMessage(jsonData.getBytes(StandardCharsets.UTF_8));
                        message.setQos(1);
                        client.publish(topic, message);

                        System.out.println("✅ 已发送数据: " + jsonData);
                        Thread.sleep(2000);
                        i.getAndIncrement();
                        System.out.println("已发送 " + i + " 条");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static MqttClient initMqttClient() throws MqttException {
        String clientId = "JavaPublisher_GRUAE_" + System.currentTimeMillis();
        MqttClient client = new MqttClient(mqttServer, clientId, null);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        System.out.println("🔗 正在连接 MQTT 服务器: " + mqttServer);
        client.connect(options);
        System.out.println("✅ 已连接到 MQTT 服务器");

        return client;
    }

    public static void main(String[] args) throws Exception {
        writeToMqtt();
    }
}
