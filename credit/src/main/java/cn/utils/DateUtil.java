package cn.utils;

import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    public static String DEFAUL_Time_TFORMAT_24hour = "yyyy-MM-dd HH:mm:ss";

    public static String Date_TFORMAT = "yyyyMMdd";

    public static String formateDateToStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(DEFAUL_Time_TFORMAT_24hour);
        return dateFormat.format(date);
    }


    public static int formateDateToInt(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(Date_TFORMAT);
        return Integer.valueOf(dateFormat.format(date));
    }

//    public static Date parseFromDateStr(String dateTimeStr) throws ParseException {
//        return DateUtils.parseDate(dateTimeStr, DEFAUL_Time_TFORMAT_24hour);
//    }

    /**
     * 昨天0时0分
     */
    public static Date getYestdayStart() {
        long current = System.currentTimeMillis();//当前时间毫秒数
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        long twelve = zero - 24 * 60 * 60 * 1000;//昨天23点59分59秒的毫秒数
        return new Date(twelve);
    }

    /**
     * 昨天23时59分
     */
    public static Date getYestdayEnd() {
        long current = System.currentTimeMillis();//当前时间毫秒数
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        long twelve = zero - 1;//昨天23点59分59秒的毫秒数
        return new Date(twelve);
    }

    /**
     * 是否是昨天
     */
    public static boolean isYestday(Date date) {
        if (date == null) {
            return false;
        }
        Date yestDay = DateUtils.addDays(new Date(), -1);
        return DateUtils.isSameDay(yestDay, date);
    }

    /**
     * 20180508->20180508-00:00
     */
    public static Date parseDayStartFromDayInt(Integer dayInt) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(Date_TFORMAT + "HHmmss");
        return dateFormat.parse(dayInt + "000000");
    }

    /**
     * 20180508->20180508-00:00
     */
    public static Date parseDayEndFromDayInt(Integer dayInt) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(Date_TFORMAT + "HHmmss");
        return dateFormat.parse(dayInt + "235959");
    }


    public static void main(String[] args) {
        System.out.println(getYestdayStart());
        System.out.println(getYestdayEnd());
        System.out.println(formateDateToInt(new Date()));
        try {
            System.out.println(parseDayStartFromDayInt(20180508));
            System.out.println(parseDayEndFromDayInt(20180508));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
