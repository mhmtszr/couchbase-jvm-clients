/*
 * Copyright (c) 2021 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.java.util;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class NodeLocatorHelperIntegrationTest extends JavaIntegrationTest {

  static private Cluster cluster;
  static private Bucket bucket;

  private NodeLocatorHelper helper;

  @BeforeAll
  static void beforeAll() {
    cluster = Cluster.connect(seedNodes(), clusterOptions());
    bucket = cluster.bucket(config().bucketname());

    bucket.waitUntilReady(Duration.ofSeconds(5));
  }

  @AfterAll
  static void afterAll() {
    cluster.disconnect();
  }

  @BeforeEach
  void setup() {
    helper = NodeLocatorHelper.create(bucket, Duration.ofMinutes(1));
  }

  @Test
  public void shouldListAllNodes() {
    assertFalse(helper.nodes().isEmpty());
  }

  @Test
  void shouldLocateActive() {
    String node = helper.activeNodeForId("foobar");
    assertTrue(helper.nodes().contains(node));
  }

  @Test
  void shouldNotAcceptHigherReplicaNum() {
    assertThrows(IllegalArgumentException.class, () -> helper.replicaNodeForId("foo", 4));
  }

  @Test
  void shouldNotAcceptLowerReplicaNum() {
    assertThrows(IllegalArgumentException.class, () -> helper.replicaNodeForId("foo", 0));
  }

}