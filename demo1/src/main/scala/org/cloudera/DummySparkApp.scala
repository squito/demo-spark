/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark

import java.io.File
import java.util.Date

import org.apache.commons.io.FileUtils
import org.apache.spark.SparkContext._  //not needed after spark 1.3

object DummySparkApp {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setAppName("Spark Count"))

    val d = sc.parallelize(1 to 1e6.toInt).cache()
    d.count()

    val sums = d.map{x => (x % 10) -> x.toLong}.reduceByKey{_ + _}
    println(sums.first)

    val path = "/Users/irashid/spark-examples/tmp_data/sums"
    val f = new File(path)
    FileUtils.deleteDirectory(f)
    sums.saveAsTextFile(path)

    val txt = sc.textFile(path)
    println(txt.count)

    println("now sleeping for 1 minute")
    println(new Date())
    Thread.sleep(60 * 1000)
  }
}