package com.algorithm.web.conf;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

	private String endpoint;

	private String accesskey;

	private String secretKey;

	private String bucketName;

	@Bean
	public MinioClient minioClient() {
		return MinioClient.builder().endpoint(endpoint).credentials(accesskey, secretKey).build();
	}

}
