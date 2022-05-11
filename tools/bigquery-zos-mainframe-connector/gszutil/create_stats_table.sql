/*
 * Copyright 2022 Google LLC All Rights Reserved.
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
CREATE TABLE `[PROJECT_ID].[DATASET_NAME].[TABLE_NAME]` (
  job_name STRING,
  job_id STRING,
  step_name STRING,
  timestamp TIMESTAMP,
  job_date DATE,
  job_time TIME,
  job_type STRING,
  bq_job_project STRING,
  bq_job_location STRING,
  bq_job_id STRING,
  job_json STRING,
  source STRING,
  destination STRING,
  rows_before_merge INT64,
  rows_read INT64,
  rows_written INT64,
  rows_affected INT64,
  rows_inserted INT64,
  rows_deleted INT64,
  rows_updated INT64,
  rows_unmodified INT64,
  statement_type STRING,
  query STRING,
  execution_ms INT64,
  queued_ms INT64,
  slot_ms INT64,
  bytes_processed INT64,
  shuffle_bytes INT64,
  shuffle_spill_bytes INT64,
  slot_utilization_rate FLOAT64,
  slot_ms_to_total_bytes_ratio FLOAT64,
  shuffle_bytes_to_total_bytes_ratio FLOAT64,
  shuffle_spill_to_shuffle_bytes_ratio FLOAT64,
  shuffle_spill_to_total_bytes_ratio FLOAT64,
  bq_stage_count INT64,
  bq_step_count INT64,
  bq_sub_step_count INT64,
  bq_stage_summary STRING,
  records_in INT64,
  records_out INT64)
PARTITION BY job_date
CLUSTER BY job_name, job_id, step_name
OPTIONS (
 partition_expiration_days=1000,
 description="Log table for mainframe jobs"),
 require_partition_filter=true
)
