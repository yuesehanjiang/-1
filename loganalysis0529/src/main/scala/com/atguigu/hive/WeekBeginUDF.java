package com.atguigu.hive;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//周计算
public class WeekBeginUDF extends UDF {

    // 计算本周的起始时间(毫秒数)
    public long evaluate() throws ParseException {
        return DateUtils.getWeekBeginTime(new Date()).getTime() ;
    }


    // 计算距离本周 offset 周的一周起始时间(毫秒数)
    public long evaluate(int offset) throws ParseException {
        return DateUtils.getWeekBeginTime(new
                Date(),offset).getTime();
    }



    // 计算某周的起始时间(毫秒数)
    public long evaluate(Date d) throws ParseException {
        return DateUtils.getWeekBeginTime(d).getTime();
    }





    // 计算距离指定周 offset 周的一周起始时间(毫秒数)
    public long evaluate(Date d,int offset) throws ParseException {
        return DateUtils.getWeekBeginTime(d,offset).getTime();
    }



    // 按照默认格式对输入的  String 类型日期进行解析，计算某周的起始时间  (毫秒 数)
    public long evaluate(String dateStr) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d = sdf.parse(dateStr);

        return DateUtils.getWeekBeginTime(d).getTime();
    }





    // 按照默认格式对输入的 String 类型日期进行解析，计算距离某周 offset 周之后 的起始时间 (毫秒数)
    public long evaluate(String dateStr,int offset) throws
            ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d = sdf.parse(dateStr);
        return DateUtils.getWeekBeginTime(d, offset).getTime();
    }



// 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算某周的起始时间(毫秒数)
    public long evaluate(String dateStr, String fmt) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt);

        Date d = sdf.parse(dateStr);

        return DateUtils.getWeekBeginTime(d).getTime();
    }




    // 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算距离某周 offset天之后的起始时间(毫秒数)
    public long evaluate(String dateStr, String fmt,int offset) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt); Date d = sdf.parse(dateStr);

        return DateUtils.getWeekBeginTime(d, offset).getTime();
    }
    public static void main(String[] args) throws ParseException {
        long evaluate = new WeekBeginUDF().evaluate("209/09/09 12:12:12","yyyy/MM/dd HH:mm:ss",3);

        System.out.println(evaluate);
        String res=null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(evaluate);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        System.out.println(res);

    }
}


