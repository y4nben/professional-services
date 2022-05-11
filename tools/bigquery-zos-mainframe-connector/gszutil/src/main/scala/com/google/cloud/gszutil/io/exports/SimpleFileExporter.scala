/*
 * Copyright 2022 Google LLC All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.gszutil.io.exports

import com.google.cloud.bigquery.{FieldList, FieldValueList}
import com.google.cloud.bqsh.cmd.Result
import com.google.cloud.gszutil.BinaryEncoder

/**
  * Simplified version of {@link com.google.cloud.gszutil.io.exports.FileExporter}.
  * It is better to refactor {@link com.google.cloud.gszutil.io.exports.FileExporter} to have interface like this one.
  * For now this one will be used with adapter from {@link com.google.cloud.gszutil.io.exports.FileExporter}.
  */
trait SimpleFileExporter {

  def validateData(schema: FieldList, encoders: Array[BinaryEncoder]): Unit

  def exportData(rows: java.lang.Iterable[FieldValueList], schema: FieldList, encoders: Array[BinaryEncoder]): Result

  def endIfOpen(): Unit

  def getCurrentExporter: FileExport
}
