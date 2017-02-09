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
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ Deserializer, Serde, Serdes, Serializer }

class GenericAvroSerde(serde: Serde[GenericRecord]) extends Serde[GenericRecord] {

  def this(client: SchemaRegistryClient, props: java.util.Map[String, _]) =
    this(Serdes.serdeFrom(new GenericAvroSerializer(client), new GenericAvroDeserializer(client, props)))

  def this(client: SchemaRegistryClient) = this(client, Collections.emptyMap[String, AnyRef])

  def this() = this(Serdes.serdeFrom(new GenericAvroSerializer, new GenericAvroDeserializer))

  def serializer: Serializer[GenericRecord] = serde.serializer

  def deserializer: Deserializer[GenericRecord] = serde.deserializer

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    serde.serializer.configure(configs, isKey)
    serde.deserializer.configure(configs, isKey)
  }

  def close(): Unit = {
    serde.serializer.close()
    serde.deserializer.close()
  }
}
