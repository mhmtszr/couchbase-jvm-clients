import com.couchbase.client.core.Core;
import com.couchbase.client.core.endpoint.QueryEndpoint;
import com.couchbase.client.core.env.CoreEnvironment;
import com.couchbase.client.core.io.NetworkAddress;
import com.couchbase.client.core.msg.query.QueryRequest;
import com.couchbase.client.core.service.ServiceContext;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.ReactiveQueryResult;
import com.couchbase.client.java.query.SimpleQuery;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class ReactiveQuerySample {

  public static void main(String... args) throws Exception {
    CoreEnvironment environment = CoreEnvironment.create("Administrator", "password");
    Core core = Core.create(environment);
    QueryEndpoint endpoint = new QueryEndpoint(new ServiceContext(core.context(),
      NetworkAddress.localhost(), 1234, ServiceType.QUERY, Optional.empty()),
      NetworkAddress.localhost(), 8093);

    endpoint.connect();

    Thread.sleep(1000);

    System.err.println(endpoint.state());


    Query query = SimpleQuery.create("select * from `travel-sample` limit 32000");
    QueryRequest request = new QueryRequest(Duration.ofSeconds(10), core.context(),
      environment.retryStrategy(), environment.credentials(), query.encode(null));

    Mono<ReactiveQueryResult> result = Mono.defer(() -> Mono.fromFuture(() -> {
      endpoint.send(request);
      return request.response().thenApply(ReactiveQueryResult::new);
    }));

    CountDownLatch latch = new CountDownLatch(1);
    BaseSubscriber<JsonObject> subscriber = new BaseSubscriber<JsonObject>() {
      @Override
      protected void hookOnSubscribe (Subscription subscription) {
        request(1);
      }

      @Override
      protected void hookOnNext (JsonObject value)
      {
        System.out.println(value);
        request(1);
      }

      @Override
      protected void hookOnComplete() {
        latch.countDown();
      }

      @Override
      protected void hookOnError(Throwable t) {
        latch.countDown();
      }
    };
    result.flux().flatMap(ReactiveQueryResult::rows).subscribe(subscriber);
    latch.await();
  }
}