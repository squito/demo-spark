package org.cloudera

import com.quantifind.sumac.{FieldArgs, ArgMain}

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Time, Seconds, StreamingContext}

class FailingStreamDemoArgs extends FieldArgs {
  var mapDelayTime = 0L
  var mapDelayBatches = 3
}

object FailingStreamDemo extends ArgMain [FailingStreamDemoArgs] {
  def main(args: FailingStreamDemoArgs): Unit = {
    val conf = new SparkConf().setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val delayTime = args.mapDelayTime
    val delayBatches = args.mapDelayBatches

    val input = new IncreasingDStream(10, ssc)

    var rddCount = 0

    input.foreachRDD{ rdd =>
      val thisRDDCount = rddCount
      println("*** Starting to process RDD : " + thisRDDCount)
      val str = rdd.map{x =>
        if ( x % 100 == 0) throw new RuntimeException("user exception on " + x)
        if (thisRDDCount % delayBatches == 0)  //create a little backlog, but make sure it can get cleared
          Thread.sleep(delayTime)
        x
      }.collect().mkString(",")
      println(str)
      rddCount += 1
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