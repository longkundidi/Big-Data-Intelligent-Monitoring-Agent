package com.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.job.utils.CMSData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataConsumerService {

    private final InfluxDBClient influxDBClient;
    private final String influxDbUrl;
    private final String influxDbToken;
    private final String influxDbOrg;
    private final String influxDbBucket;

    @Autowired
    public DataConsumerService(@Value("${influxdb.url}") String influxDbUrl,
                               @Value("${influxdb.token}") String influxDbToken,
                               @Value("${influxdb.org}") String influxDbOrg,
                               @Value("${influxdb.bucket}") String influxDbBucket) {
        //Influxdb配置
        this.influxDbUrl = influxDbUrl;
        this.influxDbToken = influxDbToken;
        this.influxDbOrg = influxDbOrg;
        this.influxDbBucket = influxDbBucket;

        this.influxDBClient = InfluxDBClientFactory.create(influxDbUrl, influxDbToken.toCharArray(), influxDbOrg);
    }

    @KafkaListener(topics = "dc_CMS")
    public void listenAlSource(ConsumerRecord<String, String> record) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CMSData cmsData = objectMapper.readValue(record.value(), CMSData.class);
            processAndStoreData(cmsData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAndStoreData(CMSData cmsData) {
        String farmName = cmsData.getFarmName();
        String turbineName = cmsData.getTurbineName();
        String location = cmsData.getLocation();
        String dataString = cmsData.getDataFloat();
        long dcTime = cmsData.getAcquisitionTime();
        long frequency = cmsData.getSampleRate();

        // 确保数据字符串不为空
        if (dataString == null || dataString.trim().isEmpty()) {
            System.out.println("Invalid data string received.");
            return;
        }

        String trimmedDataString = dataString.substring(1, dataString.length() - 1);
        String[] parts = trimmedDataString.split(", ");
        float[] dcData = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                dcData[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse value");
            }
        }

        int batchSize = 6400;
        int totalSamples = dcData.length;
        int batches = (totalSamples + batchSize - 1) / batchSize;

        System.out.println(totalSamples);
        System.out.println(batches);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        for (int batchIndex = 0; batchIndex < batches; batchIndex++) {
            List<String> stringValues = new ArrayList<>();
            int startIdx = batchIndex * batchSize;
            int endIdx = Math.min(startIdx + batchSize, totalSamples);

            for (int i = startIdx; i < endIdx; i++) {
                stringValues.add(Double.toString(dcData[i]));
            }

            String batchValue = String.join(",", stringValues);
            Instant timestamp = Instant.ofEpochMilli(dcTime).plusMillis((long) (batchIndex * batchSize / (double) frequency * 1000));
            Point point = Point
                    .measurement(farmName)
                    .addTag("turbine", turbineName)
                    .addField(location, batchValue)
                    .time(timestamp, WritePrecision.MS);

            writeApi.writePoint(influxDbBucket, influxDbOrg, point);
            System.out.println(timestamp);
        }
    }
}
