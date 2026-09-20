package sw.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestTemplateUtil {

	public static String post(String url, String body) {

		HttpHeaders httpHeaders = HttpUtil.buildHttpHeaders(MediaType.APPLICATION_JSON_VALUE);
		// httpHeaders.set("Authorization","Bearer "+token);//添加这行代码，否则报错424 Failed
		// Dependency: "{"code":1,"msg":"请求令牌已过期","data":"Full authentication is required
		// to access this resource"}"
		httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE); // 添加这一行代码,否则报错406
																		// Not Acceptable:
																		// [no body]
		HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
		RestTemplate restTemplate = HttpUtil.buildRestTemplate(HttpUtil.TIME_OUT_5_M);
		return restTemplate.postForObject(url, httpEntity, String.class);

	}

}
