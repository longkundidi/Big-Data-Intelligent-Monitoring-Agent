package com.algorithm.web.conf;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "influxdb")
public class InfluxdbConfig {

	private String url;

	private String token;

	private String org;

	private String bucket;

	@Bean("influxdbClient")
	InfluxDBClient createInfluxdbClient() {
		return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
	}

}
