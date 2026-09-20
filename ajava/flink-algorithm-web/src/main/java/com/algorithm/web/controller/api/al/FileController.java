package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.MinioService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.PathUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

	@Autowired
	private MinioService minioService;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	// 机理模型图标上传
	@PostMapping("/uploadFile1")
	public RestResult uploadFile1(MultipartFile file) {
		String path = PathUtil.buildPath(Instant.now(), "image", file.getOriginalFilename());
		return upload(file, path);
	}

	// （领域）数字孪生应用模型图标上传
	@PostMapping("/uploadFile2")
	public RestResult uploadFile2(MultipartFile file) throws Exception {
		String path = PathUtil.buildPath(Instant.now(), "image", file.getOriginalFilename());
		return upload(file, path);
	}

	// 普通算法模型图标上传
	@PostMapping("/uploadFile3")
	public RestResult uploadFile3(MultipartFile file) throws Exception {
		String path = PathUtil.buildPath(Instant.now(), "image", file.getOriginalFilename());
		return upload(file, path);
	}

	// 上传程序包
	@PostMapping("/uploadFile4")
	public RestResult uploadFile4(MultipartFile file) {
		String path = PathUtil.buildPath(Instant.now(), "program", file.getOriginalFilename());
		return upload(file, path);
	}

	RestResult upload(MultipartFile file, String path) {
		try {
			// 输入流需要关闭，使用 @Cleanup 注解，否则会报异常。
			@Cleanup
			InputStream inputStream = file.getInputStream();

			minioService.upload(inputStream, path, (long) file.getBytes().length);

		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException：" + e);
		}

		return RestResult.success(path);
	}

	@RequestMapping(value = "/downLoad_Dataset", method = { RequestMethod.GET })
	public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String filePath) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
		return ResponseEntity.ok().headers(headers).body(minioService.downloadResource(filePath));
	}

	@RequestMapping(value = "/image/**", method = { RequestMethod.GET })
	public ResponseEntity<InputStreamResource> assets(HttpServletRequest request) {
		try {
			String fileName = pathMatcher.extractPathWithinPattern("file/image/**", URLDecoder.decode(
					request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString(), "utf-8"));
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE);
			return ResponseEntity.ok().headers(headers).body(minioService.downloadResource(fileName));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = "/program/**", method = { RequestMethod.GET })
	public ResponseEntity<InputStreamResource> assetsProgram(HttpServletRequest request) {
		try {
			String fileName = pathMatcher.extractPathWithinPattern("file/program/**", URLDecoder.decode(
					request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString(), "utf-8"));
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE);
			return ResponseEntity.ok().headers(headers).body(minioService.downloadResource(fileName));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

}
