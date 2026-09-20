package com.algorithm.common.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
/*
列出 Kafka 集群中所有的主题（topics）
 */
public class KafkaTopic {
    public static Collection<String> listTopics(String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 创建AdminClient实例
        try (AdminClient admin = AdminClient.create(props)) {
            ListTopicsOptions options = new ListTopicsOptions();
            options.listInternal(false);  // 是否包含内部topics，如 __consumer_offsets

            // 使用AdminClient获取topics
            ListTopicsResult topics = admin.listTopics(options);

            // 获取topic名称的集合
            KafkaFuture<Set<String>> kafkaFuture = topics.names();
            return kafkaFuture.get();  // 阻塞调用，直到所有Topic名字可用
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();  // 发生异常时返回空列表
        }
    }

    public static void main(String[] args) {
        String bootstrapServers = "192.168.16.219:9092";
        Collection<String> topics = listTopics(bootstrapServers);

        // 输出所有Topic名字
        for (String topic : topics) {
            System.out.println(topic);
        }

        // 这里可以接着把topics列表加入到你的下拉菜单中
    }
}