package com.algorithm.web.service.impl.al;

import com.algorithm.web.conf.MinioConfig;
import com.algorithm.web.service.al.MinioService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private MinioConfig minioConfig;

	@Override
	public boolean upload(InputStream inputStream, String path, String contentType, Long size) {
		try {

			PutObjectArgs.Builder builder = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(path);

			if (contentType != null) {
				builder.contentType(contentType);
			}

			if (size != null) {
				builder.stream(inputStream, size, -1);
			}
			else {
				builder.stream(inputStream, -1, 10485760);
			}

			minioClient.putObject(builder.build());
			return true;
		}
		catch (Exception e) {
			log.error("上传文件失败: " + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean upload(InputStream inputStream, String path) {
		return upload(inputStream, path, null, null);
	}

	@Override
	public boolean upload(InputStream inputStream, String path, String contentType) {
		return upload(inputStream, path, contentType, null);
	}

	@Override
	public boolean upload(InputStream inputStream, String path, Long size) {
		return upload(inputStream, path, null, size);
	}

	@Override
	public boolean objectExist(String path) {
		try {
			minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(path).build());
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public InputStreamResource downloadResource(String fileName) {
		try {
			return new InputStreamResource(minioClient
				.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build()));
		}
		catch (Exception e) {
			log.error("文件下载失败: " + e.getMessage(), e);
			return null;
		}

	}

	public InputStream downloadResource1(String filePath) throws ServerException, InsufficientDataException,
			ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
			InvalidResponseException, XmlParserException, InternalException {
		// 假设 MinIO 存储桶名为 "my-bucket"
		String bucketName = "bigdata";
		// 获取 MinIO 上的对象内容
		return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
	}

}