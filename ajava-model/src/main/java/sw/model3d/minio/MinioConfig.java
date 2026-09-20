package sw.model3d.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

	@Value("${minio.endpoint}")
	private String endpoint;

	@Value("${minio.accesskey}")
	private String accesskey;

	@Value("${minio.secretKey}")
	private String secretKey;

	// 创建连接客户端
	@Bean
	public MinioClient minioClient() {
		MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accesskey, secretKey).build();
		return minioClient;
	}

}
