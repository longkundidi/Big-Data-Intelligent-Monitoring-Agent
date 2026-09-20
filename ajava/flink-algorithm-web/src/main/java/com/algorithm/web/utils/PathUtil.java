package com.algorithm.web.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PathUtil {

	public static String buildPath(Instant time, String prefix, String fileName) {
		ZonedDateTime zonedDateTime = time.atZone(ZoneId.systemDefault());
		return prefix + "/" + zonedDateTime.getYear() + "/" + zonedDateTime.getMonth().getValue() + "/"
				+ zonedDateTime.getDayOfMonth() + "/" + fileName;
	}

}
