/*
 * Copyright (c) 2018 Couchbase, Inc.
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

package com.couchbase.client.java.kv;

import com.couchbase.client.core.Core;
import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.msg.kv.InsertRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Stability.Internal
public enum InsertAccessor {
  ;

  public static CompletableFuture<MutationResult> insert(final Core core, final InsertRequest request) {
    core.send(request);
    return request
      .response()
      .thenApply(response -> {
        // todo: implement me
        return new MutationResult(0, Optional.empty());
      });
  }

}