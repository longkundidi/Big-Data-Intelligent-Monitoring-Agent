package sw.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class KafkaUtilNew {

	public static void writeToKafka(String kafkaServers, String topic, Long taskId, String dc_data) throws Exception { // 静态方法可以通过类名直接访问
		KafkaProducer<String, String> producer = initData(kafkaServers);
		AtomicInteger i = new AtomicInteger();

		try {
			long currentTimeMillis = System.currentTimeMillis();
			String jsonData = String.format("{\"dc_data\": \"%s\", \"dc_time\": %d , \"id\":%d}", dc_data,
					currentTimeMillis, taskId);

			ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, jsonData);
			producer.send(record);
			System.out.println("已发送数据: " + jsonData);
			Thread.sleep(1000);
			i.getAndIncrement();
			System.out.println("已发送" + i + "条");

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			producer.flush();
			producer.close();
		}
	}

	public static KafkaProducer<String, String> initData(String kafkaServers) {
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaServers);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return new KafkaProducer<>(props);
	}

}
