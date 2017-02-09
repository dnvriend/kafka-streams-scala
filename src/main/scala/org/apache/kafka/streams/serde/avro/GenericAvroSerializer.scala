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

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

class GenericAvroSerializer(client: SchemaRegistryClient) extends Serializer[GenericRecord] {
  val kafkaAvroSerializer: KafkaAvroSerializer =
    Option(client).map(client => new KafkaAvroSerializer(client)).getOrElse(new KafkaAvroSerializer)

  def this() = this(null)

  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {
    kafkaAvroSerializer.configure(configs, isKey)
  }

  def serialize(topic: String, record: GenericRecord): Array[Byte] = {
    kafkaAvroSerializer.serialize(topic, record)
  }

  def close(): Unit = {
    kafkaAvroSerializer.close()
  }
}
