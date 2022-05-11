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

package com.google.cloud.imf.gzos

import java.nio.charset.StandardCharsets

import com.google.cloud.gszutil.io.ZDataSet
import com.google.common.hash.Hashing
import org.scalatest.flatspec.AnyFlatSpec

class ZReaderSpec extends AnyFlatSpec {
  "RecordReader" should "read" in {
    val testBytes = Util.randString(100000).getBytes(StandardCharsets.UTF_8)
    val reader = new ZDataSet(testBytes, 135, 135 * 10)
    val readBytes = Util.readAllBytes(reader)
    assert(readBytes.length == testBytes.length)
    val matches = Hashing.sha256().hashBytes(testBytes).toString == Hashing.sha256().hashBytes(readBytes).toString
    assert(matches)
  }
}
