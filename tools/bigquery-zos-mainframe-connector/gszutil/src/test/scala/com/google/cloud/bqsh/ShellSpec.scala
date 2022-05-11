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

package com.google.cloud.bqsh

import com.google.cloud.bqsh.cmd.{GsZUtil, Scp}
import com.google.cloud.imf.gzos.Util
import org.scalatest.flatspec.AnyFlatSpec

class ShellSpec extends AnyFlatSpec {
  "InputParser" should "parse shell" in {
    val bqExample1 =
      """bq --project_id=project --dataset_id=dataset mk \
        |  --external_table_definition="ORC=gs://bucket/path.orc/*" \
        |  TABLE_NAME""".stripMargin
    val expected = Seq(
      "bq",
      "--project_id=project",
      "--dataset_id=dataset",
      "mk",
      "--external_table_definition=ORC=gs://bucket/path.orc/*",
      "TABLE_NAME"
    )
    val parsed = Bqsh.readArgs(bqExample1)
    assert(parsed == expected)
  }

  it should "parse shell with spaces" in {
    val bqExample1 =
      """bq --project_id project --dataset_id dataset mk \
        |  --external_table_definition "ORC=gs://bucket/path.orc/*" \
        |  TABLE_NAME""".stripMargin
    val expected = Seq(
      "bq",
      "--project_id","project",
      "--dataset_id","dataset",
      "mk",
      "--external_table_definition","ORC=gs://bucket/path.orc/*",
      "TABLE_NAME"
    )
    val parsed = Bqsh.readArgs(bqExample1)
    assert(parsed == expected)
  }

  it should "split commands" in {
    val in =
      """gsutil cp INFILE gs://bucket/path.orc
        |bq --project_id=project --dataset_id=dataset mk \
        |  --external_table_definition="ORC=gs://bucket/path.orc/*" \
        |  TABLE_NAME""".stripMargin
    val split = Bqsh.splitSH(in)
    val expected = Seq(
      "gsutil cp INFILE gs://bucket/path.orc",
      """bq --project_id=project --dataset_id=dataset mk   --external_table_definition="ORC=gs://bucket/path.orc/*"   TABLE_NAME"""
    )
    assert(split == expected)
  }

  "BQSH" should "evaluate variables" in {
    val cmd = "TABLE=project:dataset.table"
    val result = Bqsh.eval(ShCmd(cmd))
    assert(result.env.get("TABLE").contains("project:dataset.table"))
  }

  it should "replace variables" in {
    val env = Map(
      "TABLE" -> "project:dataset.table",
      "SOURCE" -> "gs://mybucket/path.orc/*"
    )
    val cmd = """bq --project_id=project --dataset_id='dataset' mk   --external_table_definition="ORC=$SOURCE" $TABLE"""
    val expected = """bq --project_id=project --dataset_id=dataset mk   --external_table_definition=ORC=gs://mybucket/path.orc/* project:dataset.table"""
    assert(Bqsh.replaceEnvVars(cmd, env) == expected)
  }

  it should "maintain env" in {

    val script =
      """TABLE=project:dataset.table
        |SOURCE=gs://mybucket/path.orc/*
        |echo $TABLE $SOURCE""".stripMargin
    val interpreter = new Bqsh.Interpreter(Util.zProvider, Map.empty, true, printCommands =
      true)
    val result = interpreter.runScript(script)
    val expected = Map[String,String](
      "TABLE" -> "project:dataset.table",
      "SOURCE" -> "gs://mybucket/path.orc/*"
    )
    assert(result.env == expected)
  }

  it should "scp args" in {
    val examples = Seq(
      Seq("--inDsn", "HLQ.DATASET.NAME"),
      Seq("--inDsn", "HLQ.DATASET.NAME", "--gcsOutUri", "gs://bucket/prefix/data.gz"),
      Seq("--inDD", "DDNAME", "--count", "1000", "--noCompress"),
    )
    for (args <- examples) {
      if (Scp.parser.parse(args, sys.env).isEmpty)
        fail(s"unable to parse ${args.mkString("'"," ","'")}")
    }
  }

  it should "gszutil args" in {
    val examples = Seq(
      Seq("--inDsn", "HLQ.DATASET.NAME",
        "--gcsOutUri", "gs://bucket/prefix/data.gz"),
      Seq("--inDsn", "HLQ.DATASET.NAME",
        "--cobDsn", "HLQ.DATASET(COPYBOOK)",
        "--gcsOutUri", "gs://bucket/prefix/data.gz"),
    )
    for (args <- examples) {
      if (GsZUtil.parser.parse(args, sys.env).isEmpty)
        fail(s"unable to parse ${args.mkString("'"," ","'")}")
    }
  }
}
