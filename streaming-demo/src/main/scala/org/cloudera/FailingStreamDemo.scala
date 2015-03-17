package org.cloudera

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Time, Seconds, StreamingContext}

object FailingStreamDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val input = new IncreasingDStream(10, ssc)

    input.foreachRDD{ rdd =>
      val str = rdd.map{x =>
        if ( x % 100 == 0) throw new RuntimeException("user exception on " + x)
        x
      }.collect().mkString(",")
      println(str)
    }

    ssc.start()
    ssc.awaitTermination(60*1000)

  }
}

class IncreasingDStream(val batchSize: Int, val _ssc: StreamingContext) extends InputDStream[Int](_ssc) {
  private var nextBatchStart = 1
  /** Method called to start receiving data. Subclasses must implement this method. */
  override def start(): Unit = {}

  /** Method called to stop receiving data. Subclasses must implement this method. */
  override def stop(): Unit = {}

  /** Method that generates a RDD for the given time */
  override def compute(validTime: Time): Option[RDD[Int]] = {
    val end = nextBatchStart + batchSize
    val start = nextBatchStart
    nextBatchStart = end
    Some(_ssc.sparkContext.parallelize(start to end))
  }
}