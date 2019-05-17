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

package com.couchbase.client.core.endpoint;

import com.couchbase.client.core.env.Credentials;
import com.couchbase.client.core.io.NetworkAddress;
import com.couchbase.client.core.io.netty.kv.ErrorMapLoadingHandler;
import com.couchbase.client.core.io.netty.kv.FeatureNegotiatingHandler;
import com.couchbase.client.core.io.netty.kv.KeyValueMessageHandler;
import com.couchbase.client.core.io.netty.kv.MemcacheProtocolDecodeHandler;
import com.couchbase.client.core.io.netty.kv.MemcacheProtocolVerificationHandler;
import com.couchbase.client.core.io.netty.kv.SaslAuthenticationHandler;
import com.couchbase.client.core.io.netty.kv.SelectBucketHandler;
import com.couchbase.client.core.io.netty.kv.ServerFeature;
import com.couchbase.client.core.service.ServiceContext;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.core.deps.io.netty.channel.ChannelPipeline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeyValueEndpoint extends BaseEndpoint {

  private final String bucketname;
  private final Credentials credentials;

  public KeyValueEndpoint(final ServiceContext ctx, final NetworkAddress hostname,
                          final int port, final String bucketname, final Credentials credentials) {
    super(hostname, port, ctx.environment().ioEnvironment().kvEventLoopGroup().get(),
      ctx, ctx.environment().ioConfig().kvCircuitBreakerConfig(), ServiceType.KV);
    this.credentials = credentials;
    this.bucketname = bucketname;
  }

  @Override
  protected PipelineInitializer pipelineInitializer() {
    return new KeyValuePipelineInitializer(endpointContext(), bucketname, credentials);
  }

  public static class KeyValuePipelineInitializer implements PipelineInitializer {

    private final EndpointContext ctx;
    private final String bucketname;
    private final Credentials credentials;

    public KeyValuePipelineInitializer(EndpointContext ctx, String bucketname, Credentials credentials) {
      this.ctx = ctx;
      this.credentials = credentials;
      this.bucketname = bucketname;
    }

    @Override
    public void init(BaseEndpoint endpoint, ChannelPipeline pipeline) {
      pipeline.addLast(new MemcacheProtocolDecodeHandler());
      pipeline.addLast(new MemcacheProtocolVerificationHandler(ctx));

      pipeline.addLast(new FeatureNegotiatingHandler(ctx, serverFeatures()));
      pipeline.addLast(new ErrorMapLoadingHandler(ctx));

      if (!ctx.environment().securityConfig().certAuthEnabled()) {
        pipeline.addLast(new SaslAuthenticationHandler(
          ctx,
          credentials.usernameForBucket(bucketname),
          credentials.passwordForBucket(bucketname)
        ));
      }

      pipeline.addLast(new SelectBucketHandler(ctx, bucketname));
      pipeline.addLast(new KeyValueMessageHandler(endpoint, ctx, bucketname));
    }

    /**
     * Returns the server features that should be negotiated.
     *
     * @return the server features to negotiate.
     */
    private Set<ServerFeature> serverFeatures() {
      Set<ServerFeature> features = new HashSet<>(Arrays.asList(
        ServerFeature.SELECT_BUCKET,
        ServerFeature.XATTR,
        ServerFeature.XERROR,
        ServerFeature.ALT_REQUEST,
        ServerFeature.SYNC_REPLICATION,
        ServerFeature.COLLECTIONS
      ));

      if (ctx.environment().ioConfig().mutationTokensEnabled()) {
        features.add(ServerFeature.MUTATION_SEQNO);
      }

      if (ctx.environment().compressionConfig().enabled()) {
        features.add(ServerFeature.SNAPPY);
      }

      return features;
    }
  }

}
