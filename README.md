# Gatling Kafka Plugin

## Install

### Maven:

Add to your `pom.xml`

```xml

<dependency>
    <groupId>io.github.amerousful</groupId>
    <artifactId>gatling-kafka</artifactId>
    <version>1.0</version>
</dependency>
```

### SBT

Add to your `build.sbt`

```scala
libraryDependencies += "io.github.amerousful" % "gatling-kafka" % "1.0"
```

Import:

```scala
import io.github.amerousful.kafka.Predef._
```

***

### Examples:

Protocol:

```scala
  val kafkaProtocol = kafka
  .broker(KafkaBroker("localhost", 9092))
  .acks("1")
  .producerIdenticalSerializer("org.apache.kafka.common.serialization.StringSerializer")
  .consumerIdenticalDeserializer("org.apache.kafka.common.serialization.StringDeserializer")
  .replyTimeout(10 seconds)
  .matchByKey()
```

Fire and forget:

```scala
  val kafkaFireAndForget = kafka("Kafka: fire and forget")
  .send
  .topic("input_topic")
  .payload(StringBody("#{payload}"))
  .key("#{key}")
  .headers(Map(
    "header_1" -> "#{h_value_1}",
    "header_2" -> "#{h_value_2}",
  ))
```

Request and reply:

```scala
  val kafkaRequestWithReply = kafka("Kafka: request with reply")
  .requestReply
  .topic("input_topic")
  .payload("""{ "m": "#{payload}" }""")
  .replyTopic("output_topic")
  .key("#{id} #{key}")
  .check(jsonPath("$.m").is("#{payload}_1"))
```

***

### How to make a request:

There are two types how to load Kafka:

1) Just send a request into a Topic without any wait, _Fire-and-forget_

```scala
kafka("Kafka: fire and forget")
  .send
  ...
```

2) Send a request into an **input** Topic, and then wait an outcome message from an **output** Topic

```scala
kafka("Kafka: request with reply")
  .requestReply
  ...
  .replyTopic("output_topic")
```

In a case with request-reply you have to define in a **protocol** waiting time for the reply:

```scala
.replyTimeout(10 seconds)
```

Another thing that you have to provide it's how to match a message. There are several options:

1) ```matchByKey()```
2) ```matchByValue()```
3) Custom matcher:

```scala

object CustomMatcher extends KafkaMatcher {
  override def requestMatchId(msg: ProducerRecord[String, String]): String = ???

  override def responseMatchId(msg: ConsumerRecord[String, String]): String = ???
}

...

.messageMatcher(CustomMatcher)

```
\
Chain for build request:
```text
send -> 
            topic() -> 
                payload() -> 
                    key() / headers() 

requestReply -> 
            topic() -> 
                payload() -> 
                        replyTopic() -> 
                            key() / headers() / check()
```

