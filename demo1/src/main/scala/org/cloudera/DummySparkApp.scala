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

import java.util.Date

import com.quantifind.sumac.validation.Required
import com.quantifind.sumac.{FieldArgs, ArgMain}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext._  //not needed after spark 1.3

class DummySparkAppArgs extends FieldArgs {
  @Required
  var path: String = _

  var appName: String = "Dummy app"

  var sleep: Boolean = false
}

object DummySparkApp extends ArgMain[DummySparkAppArgs] {
  def main(args: DummySparkAppArgs): Unit = {
    val sc = new SparkContext(new SparkConf().setAppName(args.appName))

    val d = sc.parallelize(1 to 1e6.toInt).cache().setName("initial data set")
    d.count()

    val sums = d.map{x => (x % 10) -> x.toLong}.reduceByKey{_ + _}.setName("sums")
    println(sums.first)

    val path = new Path(args.path)
    val fs: FileSystem = path.getFileSystem(sc.hadoopConfiguration)
    if (fs.exists(path)) fs.delete(path, true)
    sums.saveAsTextFile(path.toString())

    val txt = sc.textFile(path.toString())
    println(txt.count)

    if (args.sleep) {
      println("now sleeping for 1 minute")
      println(new Date())
      Thread.sleep(60 * 1000)
    }
  }
}