/*
 * Copyright 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.java.manager.collection;

import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.error.CollectionExistsException;
import com.couchbase.client.core.error.CollectionNotFoundException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.ScopeExistsException;
import com.couchbase.client.core.error.ScopeNotFoundException;
import com.couchbase.client.java.ReactiveBucket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.couchbase.client.core.Reactor.toMono;
import static com.couchbase.client.java.manager.collection.CreateCollectionOptions.createCollectionOptions;
import static com.couchbase.client.java.manager.collection.CreateScopeOptions.createScopeOptions;
import static com.couchbase.client.java.manager.collection.DropCollectionOptions.dropCollectionOptions;
import static com.couchbase.client.java.manager.collection.DropScopeOptions.dropScopeOptions;
import static com.couchbase.client.java.manager.collection.GetAllScopesOptions.getAllScopesOptions;
import static com.couchbase.client.java.manager.collection.GetScopeOptions.getScopeOptions;

/**
 * The {@link ReactiveCollectionManager} provides APIs to manage collections and scopes within a bucket.
 */
@Stability.Volatile
public class ReactiveCollectionManager {

  /**
   * The underlying async collection manager.
   */
  private final AsyncCollectionManager async;

  /**
   * Creates a new {@link ReactiveCollectionManager}.
   * <p>
   * This API is not intended to be called by the user directly, use {@link ReactiveBucket#collections()}
   * instead.
   *
   * @param async the underlying async collection manager.
   */
  @Stability.Internal
  public ReactiveCollectionManager(final AsyncCollectionManager async) {
    this.async = async;
  }

  /**
   * Creates a collection if it does not already exist.
   * <p>
   * Note that a scope needs to be created first (via {@link #createScope(String)}) if it doesn't exist already.
   *
   * @param collectionSpec the collection spec that contains the properties of the collection.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws CollectionExistsException (async) if the collection already exists
   * @throws ScopeNotFoundException (async) if the specified scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> createCollection(final CollectionSpec collectionSpec) {
    return createCollection(collectionSpec, createCollectionOptions());
  }

  /**
   * Creates a collection if it does not already exist with custom options.
   * <p>
   * Note that a scope needs to be created first (via {@link #createScope(String)}) if it doesn't exist already.
   *
   * @param collectionSpec the collection spec that contains the properties of the collection.
   * @param options the custom options to apply.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws CollectionExistsException (async) if the collection already exists
   * @throws ScopeNotFoundException (async) if the specified scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> createCollection(final CollectionSpec collectionSpec, final CreateCollectionOptions options) {
    return toMono(() -> async.createCollection(collectionSpec, options));
  }

  /**
   * Creates a scope if it does not already exist.
   *
   * @param scopeName the name of the scope to create.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws ScopeExistsException (async) if the scope already exists.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> createScope(final String scopeName) {
    return createScope(scopeName, createScopeOptions());
  }

  /**
   * Creates a scope if it does not already exist with custom options.
   *
   * @param scopeName the name of the scope to create.
   * @param options the custom options to apply.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws ScopeExistsException (async) if the scope already exists.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> createScope(final String scopeName, final CreateScopeOptions options) {
    return toMono(() -> async.createScope(scopeName, options));
  }

  /**
   * Drops a collection if it exists.
   *
   * @param collectionSpec the collection spec that contains the properties of the collection.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws CollectionNotFoundException (async) if the collection did not exist.
   * @throws ScopeNotFoundException (async) if the specified scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> dropCollection(final CollectionSpec collectionSpec) {
    return dropCollection(collectionSpec, dropCollectionOptions());
  }

  /**
   * Drops a collection if it exists with custom options.
   *
   * @param collectionSpec the collection spec that contains the properties of the collection.
   * @param options the custom options to apply.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws CollectionNotFoundException (async) if the collection did not exist.
   * @throws ScopeNotFoundException (async) if the specified scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> dropCollection(final CollectionSpec collectionSpec, final DropCollectionOptions options) {
    return toMono(() -> async.dropCollection(collectionSpec, options));
  }

  /**
   * Drops a scope if it exists.
   *
   * @param scopeName the name of the scope to drop.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws ScopeNotFoundException (async) if the scope did not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> dropScope(final String scopeName) {
    return dropScope(scopeName, dropScopeOptions());
  }

  /**
   * Drops a scope if it exists with custom options.
   *
   * @param scopeName the name of the scope to drop.
   * @param options the custom options to apply.
   * @return a {@link Mono} completing when the operation is applied or failed with an error.
   * @throws ScopeNotFoundException (async) if the scope did not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Mono<Void> dropScope(final String scopeName, final DropScopeOptions options) {
    return toMono(() -> async.dropScope(scopeName, options));
  }

  /**
   * Returns the scope if it exists.
   *
   * @param scopeName the name of the scope.
   * @return a {@link Mono} containing information about the scope.
   * @throws ScopeNotFoundException (async) if scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   * @deprecated use {@link #getAllScopes()} instead.
   */
  @Deprecated
  public Mono<ScopeSpec> getScope(final String scopeName) {
    return getScope(scopeName, getScopeOptions());
  }

  /**
   * Returns the scope if it exists with custom options.
   *
   * @param scopeName the name of the scope.
   * @param options the custom options to apply.
   * @return a {@link Mono} containing information about the scope.
   * @throws ScopeNotFoundException (async) if scope does not exist.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   * @deprecated use {@link #getAllScopes(GetAllScopesOptions)} instead.
   */
  @Deprecated
  public Mono<ScopeSpec> getScope(final String scopeName, final GetScopeOptions options) {
    return toMono(() -> async.getScope(scopeName, options));
  }

  /**
   * Returns all scopes in this bucket.
   *
   * @return a {@link Flux} with a (potentially empty) list of scopes in the bucket.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Flux<ScopeSpec> getAllScopes() {
    return getAllScopes(getAllScopesOptions());
  }

  /**
   * Returns all scopes in this bucket with custom options.
   *
   * @param options the custom options to apply.
   * @return a {@link Flux} with a (potentially empty) list of scopes in the bucket.
   * @throws CouchbaseException (async) if any other generic unhandled/unexpected errors.
   */
  public Flux<ScopeSpec> getAllScopes(final GetAllScopesOptions options) {
    return toMono(() -> async.getAllScopes(options)).flatMapMany(Flux::fromIterable);
  }

}
