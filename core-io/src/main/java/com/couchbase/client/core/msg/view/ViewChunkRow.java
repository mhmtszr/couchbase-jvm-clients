/*
 * Copyright (c) 2019 Couchbase, Inc.
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

package com.couchbase.client.core.msg.view;

import com.couchbase.client.core.msg.chunk.ChunkRow;

import java.nio.charset.StandardCharsets;

import static com.couchbase.client.core.logging.RedactableArgument.redactUser;

public class ViewChunkRow implements ChunkRow {

  private final byte[] data;

  public ViewChunkRow(byte[] data) {
    this.data = data;
  }

  public byte[] data() {
    return data;
  }

  @Override
  public String toString() {
    return "ViewChunkRow{" +
      "data=" + redactUser(new String(data, StandardCharsets.UTF_8)) +
      '}';
  }

}
