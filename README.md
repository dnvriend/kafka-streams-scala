# kafka-streams-scala
A more fluent Scala API for [kafka-streams](https://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple/)

## Disclaimer
This is work in progress

## How to use
If you want to experiment with this library, clone the repository and do `sbt publishLocal`.

## Examples

### Transforming messages from Topic A to Topic B

```scala
import com.sksamuel.avro4s.RecordFormat
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.kstream.internals.ScalaKStreamBuilder

object PersonMapper extends App {
  final case class PersonCreated(id: String, name: String, age: Int, married: Option[Boolean] = None, children: Int = 0)
  object PersonCreated {
    implicit val recordFormat = RecordFormat[PersonCreated]
  }

  ScalaKStreamBuilder(KafkaConfig.config("person-mapper"))
    .stream[String, GenericRecord]("PersonCreatedAvro")
    .parseFromAvro[PersonCreated]
    .map { event =>
      event.copy(name = "bar")
    }
    .mapToAvro
    .toTopic("MappedPersonCreatedAvro")
    .start()
}
```

### Foreach mapper

```scala
import com.sksamuel.avro4s.RecordFormat
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.kstream.internals.ScalaKStreamBuilder

import scala.language.implicitConversions

object PersonForeachMapper extends App {
  final case class PersonCreated(id: String, name: String, age: Int, married: Option[Boolean] = None, children: Int = 0)
  object PersonCreated {
    implicit val recordFormat = RecordFormat[PersonCreated]
  }

  def recordAs[A](record: GenericRecord)(implicit format: RecordFormat[A]): A = format.from(record)

  var count = 0L

  ScalaKStreamBuilder(KafkaConfig.config("person-foreach-mapper"))
    .stream[String, GenericRecord]("MappedPersonCreatedAvro")
    .parseFromAvro[PersonCreated]
    .foreach { (key, value) =>
      count += 1
      println(s"==> [PersonForeachMapper - $count] ==> key='$key', value='$value'")
    }.start()
}
```

### Configuration

```scala
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.serde.avro.GenericAvroSerde

object KafkaConfig {
  def configAsMap(applicationId: String): Map[String, String] = Map(
    // An identifier for the stream processing application. Must be unique within the Kafka cluster.
    // Each stream processing application must have a unique id. The same id must be given to all instances of the application.
    // It is recommended to use only alphanumeric characters, . (dot), - (hyphen), and _ (underscore). Examples: "hello_world", "hello_world-v1.0.0"
    //
    // This id is used in the following places to isolate resources used by the application from others:
    //
    // - As the default Kafka consumer and producer client.id prefix
    // - As the Kafka consumer group.id for coordination
    // - As the name of the sub-directory in the state directory (cf. state.dir)
    // - As the prefix of internal Kafka topic names
    // see: http://docs.confluent.io/3.1.2/streams/developer-guide.html#id24
    StreamsConfig.APPLICATION_ID_CONFIG -> applicationId,

    // A list of host/port pairs to use for establishing the initial connection to the Kafka cluster
    //
    StreamsConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
    // Zookeeper connect string for Kafka topic management
    StreamsConfig.ZOOKEEPER_CONNECT_CONFIG -> "localhost:2181",
    // key deserializer
    StreamsConfig.KEY_SERDE_CLASS_CONFIG -> Serdes.String.getClass.getName,
    // value deserializer
    StreamsConfig.VALUE_SERDE_CLASS_CONFIG -> classOf[GenericAvroSerde].getName,

    ConsumerConfig.GROUP_ID_CONFIG -> "group-foo",
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
    "schema.registry.url" -> "http://localhost:8081"
  )

  def config(applicationId: String): Properties = {
    import scala.collection.JavaConverters._
    val settings = new Properties
    settings.putAll(configAsMap(applicationId).asJava)
    settings
  }
}
```

