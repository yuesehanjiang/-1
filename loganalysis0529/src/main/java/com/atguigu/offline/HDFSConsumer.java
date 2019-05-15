package com.atguigu.offline;

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

public class HDFSConsumer {

    public static void main(String[] args) throws Exception {

        //kafka配置信息
        Properties properties = new Properties();
        properties.put("zookeeper.connect", "hadoop102:2181");
        properties.put("group.id", "g1");
        properties.put("zookeeper.session.timeout.ms", "500");
        properties.put("zookeeper.sync.time.ms", "250");
        properties.put("auto.commit.interval.ms", "1000");

        //kafka消费者
        //获取kafka连接器
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));

        //定义消费主题&消费者的线程数
        HashMap<String, Integer> topicCount = new HashMap<>();
        topicCount.put("log-analysis", 1);

        //创建数据流
        Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumerConnector.createMessageStreams(topicCount);
        KafkaStream<byte[], byte[]> stream = messageStreams.get("log-analysis").get(0);
        ConsumerIterator<byte[], byte[]> iterator = stream.iterator();

        //HDFS参数
        Configuration configuration = new Configuration();

        //HDFS文件系统
        FileSystem fs = FileSystem.get(new URI("hdfs://hadoop102:9000"), configuration, "atguigu");

        //获取当前时间
        long lastTS = System.currentTimeMillis();

        //格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM/dd/HHmm");
        String formatTime = sdf.format(new Date(lastTS));
        String parentPath = "hdfs://hadoop102:9000/";

        Path writePath = new Path(parentPath + formatTime);

        FSDataOutputStream ots = null;

        //判断路径是否存在
        if (fs.exists(writePath)) {
            ots = fs.append(writePath);
        } else {
            ots = fs.create(writePath);
        }

        //循环写出
        while (iterator.hasNext()) {
            long thisTS = System.currentTimeMillis();
            if (thisTS - lastTS > 60000) {
                //超过一分钟，写到新文件
                ots.close();
                String thisPath = parentPath + sdf.format(new Date(thisTS));
                ots = fs.create(new Path(thisPath));
                lastTS = thisTS;
            }
            //写入HDFS
            String str = new String(iterator.next().message());
            System.out.println(str);
            str = str + "\r\n";
            ots.write(str.getBytes());
            //一致性
            ots.hflush();
            ots.hsync();
        }
    }
}
