mod-rxvertx
===========

Vert.x module which uses RxJava to add support for [Reactive Extensions](https://rx.codeplex.com/) (RX) using the [RxJava](https://github.com/Netflix/RxJava/wiki) library. This allows VertX developers to use the RxJava type-safe composable API to build VertX verticles.

## Dependencies

- The module wraps the VertX core objects to add Observable support so it is tightly bound to the VertX release. 
- This module also contains the Netflix RxJava library.

## Status
Currently Observable wrappers are provided for

- EventBus
- HttpServer
- HttpClient
- NetServer
- NetClient

There are also base Observable adapters that map Handler<T> and AsyncResultHandler<T> to Observable<T> that can be used to call other Handler based APIs.

Support coming soon for

- FileSystem
- Timer
- SockJSServer

## Usage

All standard API methods of the form 

```java
void method(args...,Handler<T> handler)
```

are typically available in the form

```java
Observable<T> method(args...)
```

where the operation is executed immediately or

```java
Observable<T> observeMethod(args...)
```

where the operation is executed on subscribe. This latter form is the more 'pure' Rx method and should be used where possible (required to maintain semantics of concat eg) 
 
### EventBus

```java

RxEventBus rxEventBus = new RxEventBus(vertx.eventBus());
rxEventBus.<String>registerHandler("foo").subscribe(new Action1<RxMessage<String>>() {
  public void call(RxMessage<String> message) {
    // Send a single reply
    message.reply("pong!");
  }
});

Observable<RxMessage<String>> obs = rxEventBus.send("foo", "ping!");

obs.subscribe(
  new Action1<RxMessage<String>>() {
    public void call(RxMessage<String> message) {
      // Handle response 
    }
  },
  new Action1<Throwable>() {
    public void call(Throwable err) {
     // Handle error
    }
  }
);

```

### Helper ###
The support class `RxSupport` provides several helper methods for some standard tasks

#### Streams ####
There are two primary wrappers

##### Observable<Buffer> RxSupport.toObservable(ReadStream) ####
Convert a `ReadStream` into an `Observable<Buffer>`

##### RxSupport.stream(Observable<Buffer>,WriteStream) ####
Stream the output of an `Observable` to a `WriteStream`.

_please note that this method does not handle `writeQueueFull` so cannot be used as a pump_
