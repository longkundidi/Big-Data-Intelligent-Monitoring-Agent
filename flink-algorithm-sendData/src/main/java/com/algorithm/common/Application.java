package com.algorithm.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.algorithm.common.utils.KafkaUtilLSTMAE;
import com.algorithm.common.utils.KafkaUtilResnet;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        // 创建并启动线程来执行 KafkaUtilLSTMAE.writeToKafka()
        Thread lstmaeThread = new Thread(() -> {
            try {
                KafkaUtilLSTMAE.writeToKafka();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 创建并启动线程来执行 KafkaUtilResnet.writeToKafka()
        Thread resnetThread = new Thread(() -> {
            try {
                KafkaUtilResnet.writeToKafka();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 启动线程
        lstmaeThread.start();
        resnetThread.start();
    }
}
