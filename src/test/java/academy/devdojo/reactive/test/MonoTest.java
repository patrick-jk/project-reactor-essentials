package academy.devdojo.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. Publisher sends all the objects requested.
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
 */
@Slf4j
public class MonoTest {

    @BeforeAll
    public static void setUp() {
        BlockHound.install();
    }

//    @Test //Used to test BlockHound, will throw an exception
//    public void blockHoundWorks() {
//        Mono.delay(Duration.ofSeconds(1))
//                .doOnNext(it -> {
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .block();
//    }

    @Test
    public void monoSubscriber() {
        String name = "DevDojo Academy";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("---------------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "DevDojo Academy";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("---------------------------------");

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "DevDojo Academy";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error");
                });

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);

        log.info("---------------------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "DevDojo Academy";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace, () -> log.info("FINISHED!"));

        log.info("---------------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "DevDojo Academy";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                subscription -> subscription.request(5));

        log.info("---------------------------------");

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "DevDojo Academy";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))//will not be executed
                .doOnSuccess(s -> log.info("doOnSuccess executed {}", s));

        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace, () -> log.info("FINISHED!"));
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> log.info("Executing this doOnNext"))//will not be executed
                .log();


        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();

    }

    @Test
    public void monoOnErrorResume() {
        String name = "DevDojo Academy";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .log();


        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoOnErrorReturn() {
        String name = "DevDojo Academy";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .log();


        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();

    }
}