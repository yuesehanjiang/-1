package com.atguigu.online;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HDFS {
    //创建目录-DHADOOP_USER_NAME=root

    public static void main(String[] args) throws IOException {
        // 1 创建配置信息对象
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://hadoop101:9000");
        FileSystem fs = FileSystem.get(configuration);
        //2 创建目录
        fs.append(new Path("hdfs://hadoop101:9000/input/201905/06"));
    }


}
