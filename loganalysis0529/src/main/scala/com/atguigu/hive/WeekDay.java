package com.atguigu.hive;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
 //天计算
class DayBeginUDF extends UDF {

    // 获取当前的时刻(毫秒数)
    public   long evaluate() throws ParseException {
        return evaluate(new Date());
    }


    // 计算距离当天 offset 天之后的起始时间(毫秒数)
    public long evaluate(int offset) throws ParseException {
        return evaluate(DateUtils.getDayBeginTime(new Date(), offset));
    }
//  1556985600000      |   1556985600000   |  1557072000000
// 2019-05-05 00:00:00                       2019-05-06 00:00:00
     public static void main(String[] args) {
         String s = stampToDate("1557072000000");
         System.out.println(s);
     }


     public static String stampToDate(String s){
         String res;
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         long lt = new Long(s);
         Date date = new Date(lt);
         res = simpleDateFormat.format(date);
         return res;
     }


// 计算某天的起始时间  (毫秒数)

    public long evaluate(Date d) throws ParseException {
        return DateUtils.getDayBeginTime(d).getTime();
    }



    // 计算距离某天 offset 天之后的起始时间  (毫秒数)
    public long evaluate(Date d, int offset) throws ParseException {
        return DateUtils.getDayBeginTime(d, offset).getTime();
    }





    // 按照默认格式对输入的 String 类型日期进行解析，计算某天的起始时间  (毫秒数)
    public long evaluate(String dateStr) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d = sdf.parse(dateStr);

        return evaluate(d);
    }



    // 按照默认格式对输入的 String 类型日期进行解析，计算距离某天 offset 天之后 的起始时间 (毫秒数)
    public long evaluate(String dateStr, int offset) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d = sdf.parse(dateStr);

        return DateUtils.getDayBeginTime(d, offset).getTime();
    }




// 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算某天的起始时间(毫秒数)
    public long evaluate(String dateStr, String fmt) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt); Date d = sdf.parse(dateStr);

        return DateUtils.getDayBeginTime(d).getTime();
    }



    // 按照指定的 fmt 格式对输入的 String 类型日期进行解析，计算距离某天 offset天之后的起始时间(毫秒数)
    public long evaluate(String dateStr, String fmt, int offset) throws
            ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(fmt); Date d = sdf.parse(dateStr);

        return DateUtils.getDayBeginTime(d, offset).getTime();
    }


}

