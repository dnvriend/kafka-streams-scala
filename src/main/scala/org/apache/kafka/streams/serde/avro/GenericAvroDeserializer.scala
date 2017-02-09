/*
 * Copyright 2017 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kafka.streams.serde.avro

import java.util.Collections

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Deserializer

class GenericAvroDeserializer(client: SchemaRegistryClient, props: java.util.Map[String, _]) extends Deserializer[GenericRecord] {
  val kafkaAvroDeserializer: KafkaAvroDeserializer =
    Option(client).map(client => new KafkaAvroDeserializer(client, props)).getOrElse(new KafkaAvroDeserializer)

  def this(client: SchemaRegistryClient) = this(client, Collections.emptyMap[String, AnyRef])

  def this() = this(null, Collections.emptyMap[String, AnyRef])

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    kafkaAvroDeserializer.configure(configs, isKey)
  }

  def deserialize(s: String, bytes: Array[Byte]): GenericRecord = {
    kafkaAvroDeserializer.deserialize(s, bytes).asInstanceOf[GenericRecord]
  }

  def close(): Unit = {
    kafkaAvroDeserializer.close()
  }
}
