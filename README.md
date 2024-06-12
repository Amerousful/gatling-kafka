# Gatling Kafka Plugin [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.amerousful/gatling-kafka/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.amerousful/gatling-kafka/)

## Install

### Maven:

Add to your `pom.xml`

```xml

<dependency>
    <groupId>io.github.amerousful</groupId>
    <artifactId>gatling-kafka</artifactId>
    <version>3.2</version>
</dependency>
```

### SBT

Add to your `build.sbt`

```scala
libraryDependencies += "io.github.amerousful" % "gatling-kafka" % "3.2"
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
Scenario:
```scala
scenario("Kafka Scenario")
  .exec(kafkaFireAndForget)
```

Inject:

```scala
setUp(
  scn.inject(
    constantUsersPerSec(2) during(10 seconds)
  ).protocols(kafkaProtocol)
)
```


### [Java example](src/test/scala/KafkaJavaExample.java)
### [Kotlin example](src/test/scala/KafkaKotlinExample.kt)

***

### How to make a request:

There are three types how to load Kafka:

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

3) Only consume. 
```scala
 kafka("Kafka: Only consume")
    .onlyConsume
    .readTopic("#{output_topic}")
    .payloadForTracking {
      "payload"
    }
    .keyForTracking("key")
    .startTime("#{currentTimeMillis()}")
```

Obviously, it doesn't send any message, but as it needs to match messages, there are methods similar as for `request-reply`: `payloadForTracking`, `keyForTracking` and `header(s)ForTracking`

Another aspect to consider is the potential usefulness of tracking when a message is triggered and determining the elapsed time for its entire processing chain. To facilitate this, the method `startTime` allows you to input a specific time value in milliseconds.
```text
-- Send HTTP request
-- Write the start point into Session
-- Consume a message from Kafka
```

If startTime is not passed, it defaults to the current time. 

***
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

### Chain for build a request:
```text
send -> 
            topic() -> 
                payload() -> 
                    key() / headers() 

requestReply -> 
            topic() -> 
                payload() -> 
                        replyTopic() -> 
                            key() / headers() / check() / protobufOutput()
                            
onlyConsume -> 
            readTopic() -> 
                payloadForTracking() -> 
                            keyForTracking() / headerForTracking() / check() / startTime() / protobufOutput()
```

### Reply consumer name:
1) Static name: `.replyConsumerName("gatling-test-consumer")` 
2) If you don't define a static name it will generate by pattern `gatling-test-${java.util.UUID.randomUUID()}`

### Logs:
Add to your `logback.xml`: 
```xml
<logger name="io.github.amerousful.kafka" level="ALL"/>
```
***
## Protobuf
Starting from version 3.0, support for Protobuf payloads has been introduced. 
Please note that all examples provided assume the usage of Scala with Maven as the setup. 
This functionality works with classes generated through [ScalaPB](https://scalapb.github.io/).

Instruction:
1) Create a `.proto` file.
```bash
src/
└── test/
    └── resources/
        └── protobuf/
            └── service.proto
```

`service.proto`
```protobuf
syntax = "proto3";

package proto;

message AuthLocal {
  string token = 1;
  int32 id = 2;
  string email = 3;
}

message Order {
  string name = 1;
}
```

2) Add to the `pom.xml` Protobuf class generator.\
**Important**!  You have to define output for both Java and Scala.
```xml
...
<build>
    <testSourceDirectory>src/test/scala</testSourceDirectory>
    <plugins>
        <plugin>
            <groupId>com.github.os72</groupId>
            <artifactId>protoc-jar-maven-plugin</artifactId>
            <version>3.11.4</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <addProtoSources>all</addProtoSources>
                        <includeMavenTypes>transitive</includeMavenTypes>
                        <inputDirectories>
                            <include>src/test/</include>
                        </inputDirectories>
                        <outputTargets>

                            <outputTarget>
                                <type>java</type>
                                <addSources>test</addSources>
                                <outputDirectory>${project.basedir}/target/generated-sources/protobuf
                                </outputDirectory>
                                <pluginArtifact>com.thesamet.scalapb:protoc-gen-scala:0.11.15:sh:unix
                                </pluginArtifact>
                            </outputTarget>

                            <outputTarget>
                                <type>scalapb</type>
                                <addSources>test</addSources>
                                <outputDirectory>${project.basedir}/target/generated-sources/protobuf
                                </outputDirectory>
                                <outputOptions>java_conversions</outputOptions>
                                <pluginArtifact>com.thesamet.scalapb:protoc-gen-scala:0.11.15:sh:unix
                                </pluginArtifact>
                            </outputTarget>

                        </outputTargets>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
...
```
3) Run in a console `mvn generate-sources`.
4) Generated classes will be in the path `/target/generated-sources/protobuf`.
5) Import the generated class for use in requests and scenarios `import proto.service.AuthLocal` (it's Scala class and object)
6) Pass Scala case classes as payloads It implicitly converts Scala to Java.
```scala
.payload(
    // Scala case class with all fields
    AuthLocal("myToken", 123, "abc@xyz.com")
)
  
.payload { session =>
    val token = session("token").as[String]
    AuthLocal(token, 123, "abc@xyz.com")
}
```
7) Deserialization into Protobuf and working in the `check` with objects.
```scala
.protobufOutput(AuthLocal)
.check(
    protobufResponse((auth: AuthLocal) => auth.id) is 123
)
```
8) Protocol.
```scala

.consumerKeyDeserializer("org.apache.kafka.common.serialization.StringDeserializer")
  
.producerKeySerializer("org.apache.kafka.common.serialization.StringSerializer")
.producerValueSerializer("io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer")
  
.schemaUrl("http://localhost:8085")
```
As you see, there no needed to pass **consumer** Protobuf deserializer for **value**. Because it resolves by the `protobufOutput` method.


## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
