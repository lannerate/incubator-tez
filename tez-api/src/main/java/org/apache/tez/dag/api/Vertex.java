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

package org.apache.tez.dag.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.tez.dag.api.VertexLocationHint.TaskLocationHint;
import org.apache.tez.runtime.api.LogicalIOProcessor;
import org.apache.tez.runtime.api.TezRootInputInitializer;
import org.apache.tez.runtime.api.events.RootInputDataInformationEvent;

public class Vertex { // FIXME rename to Task

  private final String vertexName;
  private final ProcessorDescriptor processorDescriptor;

  private final int parallelism;
  private VertexLocationHint taskLocationsHint;
  private final Resource taskResource;
  private Map<String, LocalResource> taskLocalResources;
  private Map<String, String> taskEnvironment;
  private final List<RootInputLeafOutput<InputDescriptor>> additionalInputs 
                      = new ArrayList<RootInputLeafOutput<InputDescriptor>>();
  private final List<RootInputLeafOutput<OutputDescriptor>> additionalOutputs 
                      = new ArrayList<RootInputLeafOutput<OutputDescriptor>>();

  private final List<Vertex> inputVertices = new ArrayList<Vertex>();
  private final List<Vertex> outputVertices = new ArrayList<Vertex>();
  private final List<String> inputEdgeIds = new ArrayList<String>();
  private final List<String> outputEdgeIds = new ArrayList<String>();
  private String javaOpts = "";


  public Vertex(String vertexName,
      ProcessorDescriptor processorDescriptor,
      int parallelism,
      Resource taskResource) {
    this.vertexName = vertexName;
    this.processorDescriptor = processorDescriptor;
    this.parallelism = parallelism;
    this.taskResource = taskResource;
    if (parallelism < -1) {
      throw new IllegalArgumentException(
          "Parallelism should be -1 if determined by the AM"
          + ", otherwise should be >= 0");
    }
    if (taskResource == null) {
      throw new IllegalArgumentException("Resource cannot be null");
    }
  }

  public String getVertexName() { // FIXME rename to getName()
    return vertexName;
  }

  public ProcessorDescriptor getProcessorDescriptor() {
    return this.processorDescriptor;
  }

  public int getParallelism() {
    return parallelism;
  }

  public Resource getTaskResource() {
    return taskResource;
  }

  public Vertex setTaskLocationsHint(List<TaskLocationHint> locations) {
    if (locations == null) {
      return this;
    }
    assert locations.size() == parallelism;
    taskLocationsHint = new VertexLocationHint(parallelism, locations);
    return this;
  }

  // used internally to create parallelism location resource file
  VertexLocationHint getTaskLocationsHint() {
    return taskLocationsHint;
  }

  public Vertex setTaskLocalResources(Map<String, LocalResource> localResources) {
    this.taskLocalResources = localResources;
    return this;
  }

  public Map<String, LocalResource> getTaskLocalResources() {
    return taskLocalResources;
  }

  public Vertex setTaskEnvironment(Map<String, String> environment) {
    this.taskEnvironment = environment;
    return this;
  }

  public Map<String, String> getTaskEnvironment() {
    return taskEnvironment;
  }

  public Vertex setJavaOpts(String javaOpts){
     this. javaOpts = javaOpts;
     return this;
  }
  
  /**
   * Specifies an Input for a Vertex. This is meant to be used when a Vertex
   * reads Input directly from an external source </p>
   * 
   * For vertices which read data generated by another vertex - use the
   * {@link DAG addEdge} method.
   * 
   * If a vertex needs to use data generated by another vertex in the DAG and
   * also from an external source, a combination of this API and the DAG.addEdge
   * API can be used.
   * 
   * @param inputName
   *          the name of the input. This will be used when accessing the input
   *          in the {@link LogicalIOProcessor}
   * @param inputDescriptor
   *          the inputDescriptor for this input
   * @param inputInitializer
   *          An initializer for this Input which may run within the AM. This
   *          can be used to set the parallelism for this vertex and generate
   *          {@link RootInputDataInformationEvent}s for the actual Input.</p>
   *          If this is not specified, the parallelism must be set for the
   *          vertex. In addition, the Input should know how to access data for
   *          each of it's tasks. </p> If a {@link TezRootInputInitializer} is
   *          meant to determine the parallelism of the vertex, the initial
   *          vertex parallelism should be set to -1.
   * @return
   */
  public Vertex addInput(String inputName, InputDescriptor inputDescriptor,
      Class<? extends TezRootInputInitializer> inputInitializer) {
    if (additionalInputs.size() == 1) {
      throw new IllegalStateException(
          "For now, only a single Root Input can be added to a Vertex");
    }
    additionalInputs.add(new RootInputLeafOutput<InputDescriptor>(inputName,
        inputDescriptor, inputInitializer));
    return this;
  }

  /**
   * Specifies an Output for a Vertex. This is meant to be used when a Vertex
   * writes Output directly to an external destination. </p>
   * 
   * If an output of the vertex is meant to be consumed by another Vertex in the
   * DAG - use the {@link DAG addEdge} method.
   * 
   * If a vertex needs generate data to an external source as well as for
   * another Vertex in the DAG, a combination of this API and the DAG.addEdge
   * API can be used.
   * 
   * @param outputName
   *          the name of the output. This will be used when accessing the
   *          output in the {@link LogicalIOProcessor}
   * @param outputDescriptor
   * @return
   */
  // TODO : Add a processing component.
  public Vertex addOutput(String outputName, OutputDescriptor outputDescriptor) {
    additionalOutputs.add(new RootInputLeafOutput<OutputDescriptor>(outputName,
        outputDescriptor, null));
    return this;
  }

  public String getJavaOpts(){
	  return javaOpts;
  }

  @Override
  public String toString() {
    return "[" + vertexName + " : " + processorDescriptor.getClassName() + "]";
  }

  void addInputVertex(Vertex inputVertex, String edgeId) {
    inputVertices.add(inputVertex);
    inputEdgeIds.add(edgeId);
  }

  void addOutputVertex(Vertex outputVertex, String edgeId) {
    outputVertices.add(outputVertex);
    outputEdgeIds.add(edgeId);
  }

  public List<Vertex> getInputVertices() {
    return Collections.unmodifiableList(inputVertices);
  }

  public List<Vertex> getOutputVertices() {
    return Collections.unmodifiableList(outputVertices);
  }

  List<String> getInputEdgeIds() {
    return inputEdgeIds;
  }

  List<String> getOutputEdgeIds() {
    return outputEdgeIds;
  }
  
  List<RootInputLeafOutput<InputDescriptor>> getInputs() {
    return additionalInputs;
  }

  List<RootInputLeafOutput<OutputDescriptor>> getOutputs() {
    return additionalOutputs;
  }
  // FIXME how do we support profiling? Can't profile all tasks.
}
