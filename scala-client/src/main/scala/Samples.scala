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

import com.couchbase.client.core.Core
import com.couchbase.client.core.env.CoreEnvironment
import com.couchbase.client.scala.api._
import com.couchbase.client.scala.CouchbaseCluster
import com.couchbase.client.scala.document.{ReadResult, JsonObject}
import com.couchbase.client.scala.query.{N1qlQueryResult, N1qlResult}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Samples {


  def blockingApi(): Unit = {
    val cluster = CouchbaseCluster.create("localhost")
    val bucket = cluster.openBucket("default")
    val scope = bucket.openScope("scope")
    val coll = scope.openCollection("people")
    val coll2 = bucket.openCollection("scope", "people")
    // Also supported: val coll = bucket.openCollection("scope", "people")

    // As the methods below block on a Scala Future, they need an implicit ExecutionContext in scope
    implicit val ec = ExecutionContext.Implicits.global


    // All methods have both a named/default parameters version, and an [X]Options version
    val fetched1: Option[ReadResult] = coll.read("id")
    val fetched2 = coll.read("id", ReadSpec().getFullDocument)
    val fetched3 = coll.read("id", timeout = 1000.milliseconds)
    val fetched5 = coll.read("id", ReadSpec().getFullDocument, ReadOptions().timeout(1000.milliseconds))


    // ReadResult contains the raw bytes, plus metadata.  Defers conversion to the last moment.
    val getResult: ReadResult = fetched1.get
    val doc: JsonObject = getResult.contentAsObject

    case class MyUserEntity(id: String, firstName: String, age: Int)
    getResult.contentAs[MyUserEntity]("users[0]")
    getResult.users(0).getAs[User]
    getResult.users.getAs[List[User]]

    case class MyUsersEntity(users: List[User])
    val users = getResult.contentAs[MyUsersEntity]

    getResult.some.field.getString

    fetched1 match {
      case Some(ReadResult(id, content, cas, expiry)) => print("Doc has id " + id + " cas " + cas)
      case _ => println("Could not find doc")
    }


    // readOrError is a convenience method that either returns JsonDocument (no Option) or throws DocumentNotFoundException
    // TODO May remove these
    val fetched4 = coll.readOrError("id")
    val fetched7 = coll.readOrError("id", ReadOptions().timeout(1000.milliseconds))


    // getAndLock and getAndTouch work pretty much the same as get
    val fetched6 = coll.readAndLock("id", 5.seconds)


    // Simple subdoc lookup
    val resultOpt: Option[ReadResult] = coll.read("id", ReadSpec().get("field1", "field2"))
    coll.read("id", ReadSpec().get("field1", "field2"), timeout = 10.seconds)
    coll.read("id", ReadSpec().get("field1", "field2"), ReadOptions().timeout(10.seconds))


    // Parsing subdoc results.  LookupInResult is similar to a Document.
    resultOpt.foreach(result => {
      println(result.content(0).asInstanceOf[String])
      println(result.contentAsObject("field1").asInstanceOf[String])
      println(result.contentAs[String]("field1"))
      println(result.field1.asInstanceOf[String])
      case class MyProjection(field1: String, field2: Int)
      val proj = result.contentAs[MyProjection]
    })


    // JsonObject works pretty much as in SDK2, though it's now immutable
    val age: Option[Any] = fetched5.get.contentAsObject.get("age")
    val age2: Option[Int] = fetched5.get.contentAsObject.getInt("age")
    // And Scala's Dynamic feature lets us do some cool stuff:
    val age3: Int = fetched5.get.contentAsObject.age.getInt


    // Various ways of inserting
    val inserted: MutationResult = coll.insert("id", JsonObject.create)
    coll.insert("id", JsonObject.create, timeout = 1000.milliseconds, expiration = 10.days)
    coll.insert("id", JsonObject.create, timeout = 1000.milliseconds)
    coll.insert("id", JsonObject.create, InsertOptions().timeout(1000.milliseconds))
    case class User(name: String, age: Int)
    coll.insert("john", User("John", 25), timeout = 5.seconds)


    // Various ways of replacing
    if (fetched1.isDefined) {
      // JsonDocument will be an immutable Scala case class and it's trivial to copy it with different content:
      // val toReplace = fetched1.get.copy(content = JsonObject.empty())
      val toReplace = fetched1.get
      val replaced: MutationResult = coll.replace(toReplace.id, JsonObject.create, toReplace.cas)
      coll.replace(toReplace.id, JsonObject.create, toReplace.cas, timeout = 1000.milliseconds)
      coll.replace(toReplace.id, JsonObject.create, toReplace.cas, ReplaceOptions().timeout(1000.milliseconds))
      coll.replace(toReplace.id, User("John", 25), toReplace.cas, timeout = 5.seconds)
    }


    // Subdoc mutations
    val mutateResult: MutationResult = coll.mutateIn("id", MutateInSpec().insert("hello", "world").upsert("foo", "bar"))
    coll.mutateIn("id", MutateInSpec().insert("hello", "world"), MutateInOptions().timeout(10.seconds))
    coll.mutateIn("id", MutateInSpec().insert("hello", "world"), cas = 42, timeout = 10.seconds)


    // Queries
    val queryResult: N1qlQueryResult = cluster.query("select * from `beer-sample`")

    cluster.query("select * from `beer-sample` where beer = $name",
      QueryOptions().namedParameter("name", "Speckled Hen"))

    cluster.query("select * from `beer-sample` where beer = ? and producer = ?",
      QueryOptions().positionalParameters("Budweiser", "Anheuser-Busch")
        //        .scanConsistency(AtPlus(consistentWith = List(inserted.mutationToken())))
        .timeout(5.seconds))

    cluster.query("select * from `beer-sample`",
      QueryOptions().scanConsistency(StatementPlus())
        .serverSideTimeout(10.seconds))

    bucket.query(s"select * from {} where beer = 'Speckled Hen'")

    case class BeerProjection(name: String, producer: String)

    val beers1: N1qlResult[BeerProjection] = cluster.queryAs[BeerProjection]("select name, producer from `beer-sample`")
    val beers2: N1qlResult[BeerProjection] = cluster.queryAs[BeerProjection]("select {} from {}")
  }


  // There are two asynchronous APIs.  This one returns Scala Futures (similar to a Java CompletableFuture).  The API
  // is basically identical to the synchhronous one above, just returning a Future.  Most of this code is just giving
  // basic usage for Scala Futures.
  def asyncApi(): Unit = {

    // When using Scala Futures you tell them how to run (thread-pools etc.) with an ExecutionContext (similar to a
    // Java executor), normally provided as an implicit argument (e.g. all Futures below will automatically use this
    // variable, as it's in-scope, marked implicit, and has the correct type).  This basic one is a simple thread-pool.
    implicit val ec = ExecutionContext.Implicits.global

    val cluster = CouchbaseCluster.create("localhost")
    val bucket = cluster.openBucket("default")
    val scope = bucket.openScope("scope")
    val coll = scope.openCollection("people").async()

    // Gets return Future[Option[JsonDocument]].  A basic way to handle a Future's result is this:
    coll.get("id", timeout = 1000.milliseconds) onComplete {
      case Success(doc) =>
        // doc is an Option[JsonDocument]
        if (doc.isDefined) println("Got a doc!")
        else println("No doc :(")

      case Failure(err) =>
        // err is a RuntimeException
        println("Error! " + err)
    }

    // Or block on it (discouraged)
    val getFuture = coll.get("id")
    val doc = Await.result(getFuture, atMost = 5.seconds)

    // Futures are powerful and support things like map and filter.  Many of the operations supported by Project Reactor
    // are possible with Futures (though they're missing backpressure and many of Reactor's more advanced operators)
    // Get-and-replace
    val replace = coll.getOrError("id", timeout = 1000.milliseconds)
      .map(doc => {
        coll.replace(doc.id, JsonObject.empty, doc.cas, timeout = 1000.milliseconds)
      })

    Await.result(replace, atMost = 5.seconds)

    // Another, maybe tidier way of writing that get-replace
    val replace2 = for {
      doc <- coll.getOrError("id", timeout = 1000.milliseconds)
      doc <- {
        // coll.replace(doc.copy(content = JsonObject.empty()))
        coll.replace(doc.id, JsonObject.create, doc.cas)
      }
    } yield doc

    Await.result(replace, atMost = 5.seconds)

    // Insert
    coll.insert("id", JsonObject.create, timeout = 1000.milliseconds) onComplete {
      case Success(doc) =>
      case Failure(err) =>
    }

  }


  // Finally, this API wraps the reactive library Project Reactor
  // The API is basically identical to the blocking one except returning Reactor Mono's.  Most of this code is showing
  // normal Reactor usage.
  // Disabled for now to keep up with rapid prototyping, but it'll look something like this
  //  def reactiveAPI(): Unit = {
  //    val cluster = CouchbaseCluster.create("localhost")
  //    val bucket = cluster.openBucket("default")
  //    val scope = new Scope(cluster, bucket, "scope")
  //    val coll = scope.openCollection("people").reactive()
  //
  //    // As the methods below wrap a Scala Future, they need an implicit ExecutionContext in scope
  //    implicit val ec = ExecutionContext.Implicits.global
  //
  //    // Get
  //    coll.get("id", timeout = 1000.milliseconds)
  //      .map(doc => {
  //        if (doc.isDefined) println("Got doc")
  //        else println("No doc :(")
  //      })
  //      // As normal with Reactive, blocking is discouraged - just for demoing
  //      .block()
  //
  //    // Get-replace
  //    coll.getOrError("id", timeout = 1000.milliseconds)
  //      .flatMap(doc => {
  //        // val newDoc = doc.copy(content = JsonObject.empty())
  //        val newDoc = doc
  //        coll.replace(newDoc)
  //      })
  //      // As normal with Reactive, blocking is discouraged - just for demoing
  //      .block()
  //  }
}