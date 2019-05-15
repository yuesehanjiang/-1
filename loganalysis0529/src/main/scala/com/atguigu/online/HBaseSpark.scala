package com.atguigu.online

import java.util.Properties

import com.atguigu.ll.model.{StartupReportLogs, UserCityStatModel}
import org.apache.spark.SparkConf
import com.atguigu.ll.registrator.MyKryoRegistrator
import com.atguigu.ll.service.BehaviorStatService
//import com.atguigu.ll.utils.{JSONUtil, PropertiesUtil}
import kafka.common.{OffsetAndMetadata, TopicAndPartition}
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaCluster.Err
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaCluster, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object HBaseSpark {

  def main(args: Array[String]): Unit = {

    //1.创建spark配置信息
    /*  val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("HBaseSpark")
    //使用Kryo序列化库，如果要使用Java序列化库，需要把该行屏蔽掉//使用Kryo序列化库，如果要使用Java序列化库，需要把该行屏蔽掉
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    //在Kryo序列化库中注册自定义的类集合，如果要使用Java序列化库，需要把该行屏蔽掉
    sparkConf.set("spark.kryo.registrator", classOf[MyKryoRegistrator].getName)

    //2.创建ssc
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    //kafka参数
   // val properties: Properties = PropertiesUtil.getProperties("E:\\WorkSpace_IDEA\\loganalysis0529\\src\\main\\resources\\config.properties")
  /*  val brokers: String = properties.getProperty("kafka.broker.list")
    val topic: String = properties.getProperty("kafka.topic")
    val group: String = properties.getProperty("kafka.groupId")*/

/*
    val kafkaPara = Map(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> group,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer")

    //获取KafkaCluster
    val kafkaCluster = new KafkaCluster(kafkaPara)

    val fromOffset: Map[TopicAndPartition, Long] = getOffset(kafkaCluster, group, topic)
*/

    //读取Kafka数据创建DStream
   /* val kafkaDStream: InputDStream[String] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, String](
      ssc,
      kafkaPara,
      fromOffset,
      (x: MessageAndMetadata[String, String]) => x.message()
    )*/

    //获取当前的offset
    var ranges: Array[OffsetRange] = Array[OffsetRange]()
    val oriDStream: DStream[String] = kafkaDStream.transform(rdd => {
      ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    })

    //处理数据(1.过滤)
    val filterDStream: DStream[String] = oriDStream.filter(message => {
      if (!message.contains("appVersion") && !message.contains("startTimeInMs")) {
        false
      } else {
        true
      }
    })

    //解析数据，封装KV（city，1）
    val cityToOne: DStream[(String, Int)] = filterDStream.map(message => {
      //将JSON数据转换为对象
      val log: StartupReportLogs = JSONUtil.json2Object[StartupReportLogs](message, classOf[StartupReportLogs])

      val city: String = log.getCity

      (city, 1)
    })

    //统计每个城市的点击次数
    val cityToCount: DStream[(String, Int)] = cityToOne.reduceByKey(_ + _)

    //写往HBase
    cityToCount.foreachRDD(rdd => {
      rdd.foreachPartition(messages => {
        while (messages.hasNext) {
          //获取HBase连接
          val service: BehaviorStatService = BehaviorStatService.getInstance(properties)
          //封装HBASE操作对象
          val model = new UserCityStatModel
          val cityCount: (String, Int) = messages.next()
          //赋值
          model.setCity(cityCount._1)
          model.setCount(cityCount._2)
          model.setNum(cityCount._2.toLong)
          //写入
          service.addUserNumOfCity(model)
        }
      })
      //提交offset
      offsetToZK(kafkaCluster, group, ranges)
    })

    ssc.start()
    ssc.awaitTermination()
  }

  //提交offset
  def offsetToZK(kafkaCluster: KafkaCluster, group: String, ranges: Array[OffsetRange]): Unit = {

    //声明存储offset的map
    var partitionToMetadata: Map[TopicAndPartition, OffsetAndMetadata] = Map[TopicAndPartition, OffsetAndMetadata]()

    ranges.foreach(x => {
      partitionToMetadata += (x.topicAndPartition() -> OffsetAndMetadata(x.untilOffset))
    })

    //提交
    kafkaCluster.setConsumerOffsetMetadata(group, partitionToMetadata)
  }

  //读取ZK中保存的offset
  def getOffset(kafkaCluster: KafkaCluster, group: String, topic: String): _root_.scala.Predef.Map[_root_.kafka.common.TopicAndPartition, Long] = {

    //定义一个map集合接收每个分区的offset
    var partitionToLong: Map[TopicAndPartition, Long] = Map[TopicAndPartition, Long]()

    //获取所消费的主题中所有分区
    val topicAndPartition: Either[Err, Set[TopicAndPartition]] = kafkaCluster.getPartitions(Set(topic))

    //获取一下zk中的offset（可能没有值）
    val topicAndPartitionEither: Either[Err, Map[TopicAndPartition, Long]] = kafkaCluster.getConsumerOffsets(group, topicAndPartition.right.get)

    if (topicAndPartitionEither.isLeft) {
      //第一次消费
      for (elem <- topicAndPartition.right.get) {
        partitionToLong += (elem -> 0L)
      }
    } else {
      //非第一次消费，在ZK中有值
      partitionToLong = topicAndPartitionEither.right.get
    }
    partitionToLong
  }*/


  }
}