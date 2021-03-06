/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tez.dag.app.taskclean;

import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.v2.api.records.TaskAttemptId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.event.AbstractEvent;

/**
 * This class encapsulates task cleanup event.
 * 
 */
public class TaskAttemptCleanupEvent extends
    AbstractEvent<TaskCleaner.EventType> {

  private final TaskAttemptId attemptID;
  private final OutputCommitter committer;
  private final TaskAttemptContext attemptContext;
  private final ContainerId containerId;

  public TaskAttemptCleanupEvent(TaskAttemptId attemptID,
      ContainerId containerId, OutputCommitter committer,
      TaskAttemptContext attemptContext) {
    super(TaskCleaner.EventType.TASK_CLEAN);
    this.attemptID = attemptID;
    this.containerId = containerId;
    this.committer = committer;
    this.attemptContext = attemptContext;
  }

  public TaskAttemptId getAttemptID() {
    return attemptID;
  }

  public OutputCommitter getCommitter() {
    return committer;
  }

  public TaskAttemptContext getAttemptContext() {
    return attemptContext;
  }

  /**
   * containerId could be null if the container task attempt had not started.
   * @return
   */
  public ContainerId getContainerId() {
    return containerId;
  }

}
