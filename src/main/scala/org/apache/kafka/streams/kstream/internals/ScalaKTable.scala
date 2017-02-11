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

package org.apache.kafka.streams.kstream.internals

import com.sksamuel.avro4s.RecordFormat
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.ProcessorSupplier
import play.api.libs.json.{ Format, Json }

import scala.collection.JavaConverters._
import scala.language.implicitConversions

trait ScalaKTable[K, V] extends KTable[K, V] {
  def filter(p: (K, V) => Boolean): ScalaKTable[K, V]
  def map[V1](f: V => V1): ScalaKTable[K, V1]
  def parseFromAvro[V1](implicit recordFormat: RecordFormat[V1]): ScalaKTable[K, V1]
  def parseFromJson[V1](implicit format: Format[V1]): ScalaKTable[K, V1]
  def mapToAvro(implicit recordFormat: RecordFormat[V]): ScalaKTable[K, GenericRecord]
  def mapToJson(implicit format: Format[V]): ScalaKTable[K, String]
  def foreachScalaDsl(f: (K, V) => Unit): ScalaKStreamBuilder
  def toTopic(topic: String): ScalaKStreamBuilder
}

object ScalaKTable {
  final val FILTER_NAME = "KTABLE-FILTER-"
  final val FOREACH_NAME = "KTABLE-FOREACH-"
  final val JOINTHIS_NAME = "KTABLE-JOINTHIS-"
  final val JOINOTHER_NAME = "KTABLE-JOINOTHER-"
  final val LEFTTHIS_NAME = "KTABLE-LEFTTHIS-"
  final val LEFTOTHER_NAME = "KTABLE-LEFTOTHER-"
  final val MAPVALUES_NAME = "KTABLE-MAPVALUES-"
  final val MERGE_NAME = "KTABLE-MERGE-"
  final val OUTERTHIS_NAME = "KTABLE-OUTERTHIS-"
  final val OUTEROTHER_NAME = "KTABLE-OUTEROTHER-"
  final val PRINTING_NAME = "KSTREAM-PRINTER-"
  final val SELECT_NAME = "KTABLE-SELECT-"
  final val SOURCE_NAME = "KTABLE-SOURCE-"
  final val TOSTREAM_NAME = "KTABLE-TOSTREAM-"

  implicit def function2Predicate[K, V](p: (K, V) => Boolean): Predicate[K, V] = new Predicate[K, V] {
    override def test(key: K, value: V): Boolean = p(key, value)
  }

  implicit def toForeachAction[K, V](f: (K, V) => Unit): ForeachAction[K, V] = new ForeachAction[K, V] {
    override def apply(key: K, value: V): Unit = f(key, value)
  }
}

class ScalaKTableImpl[K, S, V](topology: KStreamBuilder, name: String, processorSupplier: ProcessorSupplier[_, _], sourceNodes: Set[String], storeName: String, dsl: ScalaKStreamBuilder)
    extends KTableImpl[K, S, V](topology, name, processorSupplier, sourceNodes.asJava, storeName) with ScalaKTable[K, V] {

  import ScalaKTable._

  def filter(p: (K, V) => Boolean): ScalaKTable[K, V] = {
    val name = topology.newName(FILTER_NAME)
    val processorSupplier = new KTableFilter[K, V](this, p, false)
    topology.addProcessor(name, processorSupplier, this.name)
    new ScalaKTableImpl(topology, name, processorSupplier, sourceNodes, storeName, dsl)
  }

  def map[V1](f: V => V1): ScalaKTable[K, V1] = {
    val name = topology.newName(MAPVALUES_NAME)
    val processorSupplier = new KTableMapValues[K, V, V1](this, new ValueMapper[V, V1] {
      override def apply(value: V): V1 = f(value)
    })
    topology.addProcessor(name, processorSupplier, this.name)
    new ScalaKTableImpl(topology, name, processorSupplier, sourceNodes, storeName, dsl)
  }

  def parseFromAvro[V1](implicit recordFormat: RecordFormat[V1]): ScalaKTable[K, V1] = map {
    case v: GenericRecord => recordFormat.from(v)
    case _                => throw new IllegalArgumentException("Element in stream is not of type GenericRecord")
  }

  def parseFromJson[V1](implicit format: Format[V1]): ScalaKTable[K, V1] = map {
    case v: String => Json.parse(v).as[V1]
    case _         => throw new IllegalArgumentException("Element in stream is not of type String")
  }

  def mapToAvro(implicit recordFormat: RecordFormat[V]): ScalaKTable[K, GenericRecord] =
    map(recordFormat.to)

  def mapToJson(implicit format: Format[V]): ScalaKTable[K, String] =
    map(value => Json.toJson(value).toString)

  def foreachScalaDsl(f: (K, V) => Unit): ScalaKStreamBuilder = {
    super.foreach(f)
    dsl
  }

  def toTopic(topic: String): ScalaKStreamBuilder = {
    super.to(topic)
    dsl
  }

}
