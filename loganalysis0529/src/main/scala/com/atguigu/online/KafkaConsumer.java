package com.atguigu.online;


import com.atguigu.ll.model.StartupReportLogs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

public class KafkaConsumer {

    private static Configuration configuration = new Configuration();
    private static FileSystem fs = null;
    private static FSDataOutputStream outputStream = null;
    private static Path writePath = null;
    private static String hdfsBasicPath = "hdfs://hadoop101:8020/input/";

    public static void main(String[] args) {
// 创建配置
        Properties properties = new Properties();
        properties.put("zookeeper.connect", "hadoop101:2181");
        properties.put("group.id", "group1");
        properties.put("zookeeper.session.timeout.ms", "1000");
        properties.put("zookeeper.sync.time.ms", "250");
        properties.put("auto.commit.interval.ms", "1000");

// 创建消费者连接器
// 消费者客户端会通过消费者连接器（ConsumerConnector）连接 ZK 集群
// 获取分配的分区，创建每个分区对应的消息流（MessageStream），最后迭代
        // 消息流，读取每条消息
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));

        try {



            // 1 创建配置信息对象

            configuration.set("fs.defaultFS", "hdfs://hadoop101:8020");
             fs = FileSystem.get(configuration);

            System.out.println("fs::"+fs);
               outputStream = fs.create(new Path("hdfs://hadoop101:8020/input/hahhahahh/"));

            //2 创建目录
           // fs.mkdirs(new Path("hdfs://hadoop101:9000/user1/hah"));
// 获取 HDFS 文件系统
           /* fs = FileSystem.get(new
                    URI("hdfs://hadoop101:8020"), configuration, "root");*/


            System.out.println(fs);
        } catch (Exception e) {
            e.printStackTrace();


        }

// 创建 HashMap 结构，用于指定消费者订阅的主题和每个主题需要的消费者线程 数
// Key：主题名称 Value：消费线程个数
// 一个消费者可以设置多个消费者线程，一个分区只会被分配给一个消费者线程
        HashMap<String, Integer> topicCount = new HashMap<>();

// 消费者采用多线程访问的分区都是隔离的，所以不会出现一个分区被不同线程同 时访问的情况
// 在上述线程模型下，消费者连接器负责处理分区分配和拉取消息

// 每一个 Topic 至少需要创建一个 Consumer thread
// 如果有多个 Partitions，则可以创建多个 Consumer thread 线程
// Consumer thread 数量 > Partitions 数量，会有 Consumer thread 空


// 设置每个主题的线程数量
// 设置 analysis-test 的线程数量为 1
        topicCount.put("tc", 1);

// 每个消费者线程都对应了一个消息流
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =
                consumer.createMessageStreams(topicCount);
        System.out.println(consumerMap);
// 消费者迭代器，从消息流中取出数据
// consumerMap.get("analysis-test")获取 analysis-test 主题的所有

// 由于只有一个线程，只有一个消息流，因此 get(0)获取这个唯一的消息流
        KafkaStream<byte[], byte[]> stream =
                consumerMap.get("tc").get(0);

// 获取 MessageStream 中的数据迭代器
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        String message=new String(it.next().message());
        System.out.println("message::::"+message);
// 获取当前时间
        Long lastTime = System.currentTimeMillis();

// 获取数据写入全路径
        String totalPath = getTotalPath(lastTime);

// 根据路径创建 Path 对象
        writePath = new Path(totalPath);

// 创建文件流
        try {
            if (fs.exists(writePath)) {
                outputStream = fs.append(writePath);
            } else {
                outputStream = fs.create(writePath, true);
            }
        } catch (Exception e) {


            e.printStackTrace();
        }

        while (it.hasNext()) {
// 收集两分钟的数据后更换目录
            if (System.currentTimeMillis() - lastTime > 6000) {
                try {
                    outputStream.close();
                    Long currentTime = System.currentTimeMillis();
                    String newPath = getTotalPath(currentTime);
                    writePath = new Path(newPath);
                    outputStream = fs.create(writePath);
                    lastTime = currentTime;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String jasonStr = new String(it.next().message());
            System.out.println("我是数据啊：：："+jasonStr);
            if (jasonStr.contains("appVersion")) {
                System.out.println("startupPage");
                //转成\t 数据
                 Gson gson=new Gson();
                StartupReportLogs logs=    gson.fromJson(jasonStr,new TypeToken<StartupReportLogs>(){}.getType());

             String startLog=logs.getAppVersion()+"\t"+logs.getStartTimeInMs()+"\t"+logs.getActiveTimeInMs()+"\t"+
                     logs.getCity()+"\t"+logs.getUserId()+"\t"+logs.getAppId()+"\t"+logs.getAppPlatform();
                save(startLog);
            } else if (jasonStr.contains("currentPage")) {
                System.out.println("PageLog");
            } else {
                System.out.println("ErrorLog");
            }
        }
    }

    private static void save(String log) {
        try {
// 将日志内容写入 HDFS 文件系统
            String logEnd = log + "\r\n";
            outputStream.write(logEnd.getBytes());
// 一致性模型
            outputStream.hflush();
            outputStream.hsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String timeTransform(Long timeInMills) {
        Date time = new Date(timeInMills);
        String formatDate = "";
        try {
            SimpleDateFormat sdf = new
                    SimpleDateFormat("yyyyMM-dd-HHmm");
            formatDate = sdf.format(time);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return formatDate;
    }

    private static String getDirectoryFromDate(String date) {
        String[] directories = date.split("-");
        String directory = directories[0] + "/" + directories[1];
        return directory;
    }

    private static String getFileName(String date) {
        String[] dateSplit = date.split("-");
        String fileName = dateSplit[2];
        return fileName;
    }

    private static String getTotalPath(Long lastTime) {
// 时间格式转换
        String formatDate = timeTransform(lastTime);
// 提取目录
        String directory = getDirectoryFromDate(formatDate);
// 提取文件名称
        String fileName = getFileName(formatDate);
// 全路径
        String totalPath = hdfsBasicPath + directory + "/" + fileName;

        return totalPath;
    }
}


