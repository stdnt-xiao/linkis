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

package org.apache.linkis.engineplugin.spark.datacalc.source

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineplugin.spark.datacalc.api.DataCalcSource

import org.apache.commons.text.StringSubstitutor
import org.apache.spark.sql.{Dataset, Row, SparkSession}

class FileSource extends DataCalcSource[FileSourceConfig] with Logging {

  override def getData(spark: SparkSession): Dataset[Row] = {
    val reader = spark.read

    if (config.getOptions != null && !config.getOptions.isEmpty) {
      reader.options(config.getOptions)
    }
    val substitutor = new StringSubstitutor(config.getVariables)
    val path = substitutor.replace(config.getPath)
    logger.info(s"Load data from file <$path>")

    var df = config.getSerializer match {
      case "csv" => reader.csv(path)
      case "json" => reader.json(path)
      case "parquet" => reader.parquet(path)
      case "text" => reader.text(path)
      case "orc" => reader.orc(path)
      case _ => reader.format(config.getSerializer).load(path)
    }
    if (config.getColumnNames != null && config.getColumnNames.length > 0) {
      df = df.toDF(config.getColumnNames: _*)
    }
    df
  }

}
