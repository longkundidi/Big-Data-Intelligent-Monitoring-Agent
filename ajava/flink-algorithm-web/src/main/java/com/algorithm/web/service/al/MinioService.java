package com.algorithm.web.service.al;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MinioService {

	final MinioClient minioClient = null;

	boolean upload(InputStream inputStream, String path, String contentType, Long size);

	boolean upload(InputStream inputStream, String path);

	boolean upload(InputStream inputStream, String path, String contentType);

	boolean upload(InputStream inputStream, String path, Long size);

	boolean objectExist(String path);

	InputStreamResource downloadResource(String fileName);

	InputStream downloadResource1(String fileName) throws ServerException, InsufficientDataException,
			ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
			InvalidResponseException, XmlParserException, InternalException;

}
