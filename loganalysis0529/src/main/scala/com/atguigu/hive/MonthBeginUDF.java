package com.atguigu.hive;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonthBeginUDF extends UDF {

    // 计算本月的起始时间(毫秒数)
    public long evaluate() throws ParseException {
        return DateUtils.getMonthBeginTime(new Date()).getTime() ;
    }

    public static void main(String[] args) throws ParseException {
        long evaluate = new MonthBeginUDF().evaluate();

        System.out.println(evaluate);
    }

    // 计算距离本月 offset 个月的月起始时间(毫秒数)1556640000000

    public long evaluate(int offset) throws ParseException {
        return DateUtils.getMonthBeginTime(new
                Date(),offset).getTime();
    }

    // 计算某月的起始时间(毫秒数)
    public long evaluate(Date d) throws ParseException {
        return DateUtils.getMonthBeginTime(d).getTime();
    }

    // 计算距离指定月 offset 个月的月起始时间(毫秒数)
    public long evaluate(Date d,int offset) throws ParseException {
        return DateUtils.getMonthBeginTime(d,offset).getTime();
    }

    // 按照默认格式对输入的  String 类型日期进行解析，计算某月的起始时间  (毫秒 数)
    public long evaluate(String dateStr) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        Date d = sdf.parse(dateStr);

        return DateUtils.getMonthBeginTime(d).getTime();
    }


    // 按照默认格式对输入的 String 类型日期进行解析，计算距离某月 offset 个月之后的起始时间 (毫秒数)
    public long evaluate(String dateStr,int offset) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        Date d = sdf.parse(dateStr);

        return DateUtils.getMonthBeginTime(d, offset).getTime();
    }

// 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算某月的起始时(毫秒数)
    public long evaluate(String dateStr, String fmt) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt); Date d = sdf.parse(dateStr);

        return DateUtils.getMonthBeginTime(d).getTime();
    }

    // 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算距离某月 offset月之后的起始时间(毫秒数)
    public long evaluate(String dateStr, String fmt,int offset) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt); Date d = sdf.parse(dateStr);

        return DateUtils.getMonthBeginTime(d, offset).getTime();
    }
}



