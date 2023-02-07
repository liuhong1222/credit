package cn.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

	/**
	 * 判断集合是否为空
	 * 
	 * @param collection
	 * @return
	 */
	public static Boolean isNotEmpty(Collection<?> collection) {
		return (null == collection || collection.size() <= 0);
	}
	
	/**
     * 判断是否为正常的手机号码
     * @param str
     * @return
     */
    public static boolean isMobile(String str) {
    	String pattern = "^(13|14|15|16|17|18|19)[0-9]{9}$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(str);
		return m.matches();
    }

	/**
	 * 判断字符是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isNotString(String str) {
		return (null == str || "".equals(str) || "null".equals(str));
	}

	/**
	 * 验证是否为13位有效数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if (str.length() != 11) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		System.out.println(CommonUtils.isNotEmpty(null));
		System.out.println(CommonUtils.isNotString(""));
		System.out.println(CommonUtils.isNumeric("~！@#￥%……&*（）——"));
	}

	
}
