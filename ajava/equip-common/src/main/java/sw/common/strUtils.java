package sw.common;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些常用的字符串方法定义
 */
public class strUtils {

	/**
	 * str在data中第num次出现的位置
	 * @param data
	 * @param str
	 * @param num
	 * @return
	 */
	public static int getIndexOf(String data, String str, int num) {
		Pattern pattern = Pattern.compile(str);
		Matcher findMatcher = pattern.matcher(data);
		// 标记遍历字符串的位置
		int indexNum = 0;
		while (findMatcher.find()) {
			indexNum++;
			if (indexNum == num) {
				break;
			}
		}
		return findMatcher.start();
	}

	/**
	 * key在str中数目
	 * @param str
	 * @param key
	 * @return
	 */
	public static int getCount(String str, String key) {
		if (str == null || key == null || "".equals(str.trim()) || "".equals(key.trim())) {
			return 0;
		}
		int count = 0;
		int index = 0;
		while ((index = str.indexOf(key, index)) != -1) {
			index = index + key.length();
			count++;
		}
		return count;
	}

	/**
	 * 数组去重
	 * @param str
	 * @return
	 */
	public static String[] strArrRemoveDup(String[] str) {
		Set<String> set = new HashSet<>();
		for (int i = 0; i < str.length; i++) {
			set.add(str[i]);
		}
		return (String[]) set.toArray(new String[set.size()]);
	}

	/**
	 * 提取文件名和后缀
	 * @param fileName
	 * @return
	 */
	public static String[] resolveFileName(String fileName) {
		String[] str = new String[2];
		if (!("".equals(fileName))) {
			str[0] = fileName.substring(0, fileName.lastIndexOf(".")); // 取文件名
			str[1] = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()); // 取文件后缀名
			return str;
		}
		return null;
	}

	/**
	 * 文件名添加时间戳
	 * @param fileName
	 * @return
	 */
	public static String timestampFileName(String fileName) {
		String[] str = resolveFileName(fileName);
		if (str != null)
			return str[0] + "_" + new Date().getTime() + "." + str[1];
		return null;
	}

	/**
	 * 正则表达式去掉匹配成功的字符串
	 * @param pattern: 正则表达式
	 * @param str: 待处理字符串
	 * @return
	 */
	public static String getSubString(String pattern, String str) {
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		if (m.find())
			return str.substring(m.end(), str.length());
		else
			return null;
	}

}
