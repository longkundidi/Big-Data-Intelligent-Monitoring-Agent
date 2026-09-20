package sw.utils;

import com.alibaba.fastjson.JSON;

public class JsonUtil {

	public static String toJson(Object obj) {
		return JSON.toJSONString(obj);
	}

	public static <T> T fromJson(String jsonString, Class<T> clazz) {
		return JSON.parseObject(jsonString, clazz);
	}

}
