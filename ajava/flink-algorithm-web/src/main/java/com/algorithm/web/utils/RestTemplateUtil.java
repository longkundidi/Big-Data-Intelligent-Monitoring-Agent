package com.algorithm.web.utils;

import com.algorithm.web.common.util.HttpUtil;
import com.algorithm.web.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestTemplateUtil {

	public static String post(String url, String body) {
		try {
			if (StringUtils.isEmpty(url) || StringUtils.isEmpty(body)) {
				log.error("url or body is null url={} body={}", url, body);
				throw new BizException("请求参数url or body is null");
			}
			HttpHeaders httpHeaders = HttpUtil.buildHttpHeaders(MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
			RestTemplate restTemplate = HttpUtil.buildRestTemplate(HttpUtil.TIME_OUT_5_M);
			return restTemplate.postForObject(url, httpEntity, String.class);
		}
		catch (Exception e) {
			log.error("发送失败: {}", e.getMessage());
			return "发送失败: " + e.getMessage();
		}
	}

}
