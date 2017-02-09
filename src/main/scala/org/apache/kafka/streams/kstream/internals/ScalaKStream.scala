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
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream._
import play.api.libs.json.{ Format, Json }

import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, ExecutionContext, Future }

object ScalaKStream {
  final val BRANCH_NAME = "KSTREAM-BRANCH-"
  final val BRANCHCHILD_NAME = "KSTREAM-BRANCHCHILD-"
  final val FILTER_NAME = "KSTREAM-FILTER-"
  final val FLATMAP_NAME = "KSTREAM-FLATMAP-"
  final val FLATMAPVALUES_NAME = "KSTREAM-FLATMAPVALUES-"
  final val JOINTHIS_NAME = "KSTREAM-JOINTHIS-"
  final val JOINOTHER_NAME = "KSTREAM-JOINOTHER-"
  final val LEFTJOIN_NAME = "KSTREAM-LEFTJOIN-"
  final val MAP_NAME = "KSTREAM-MAP-"
  final val MAPVALUES_NAME = "KSTREAM-MAPVALUES-"
  final val MERGE_NAME = "KSTREAM-MERGE-"
  final val OUTERTHIS_NAME = "KSTREAM-OUTERTHIS-"
  final val OUTEROTHER_NAME = "KSTREAM-OUTEROTHER-"
  final val PROCESSOR_NAME = "KSTREAM-PROCESSOR-"
  final val PRINTING_NAME = "KSTREAM-PRINTER-"
  final val KEY_SELECT_NAME = "KSTREAM-KEY-SELECT-"
  final val SINK_NAME = "KSTREAM-SINK-"
  final val SOURCE_NAME = "KSTREAM-SOURCE-"
  final val TRANSFORM_NAME = "KSTREAM-TRANSFORM-"
  final val TRANSFORMVALUES_NAME = "KSTREAM-TRANSFORMVALUES-"
  final val WINDOWED_NAME = "KSTREAM-WINDOWED-"
  final val FOREACH_NAME = "KSTREAM-FOREACH-"
  final val REPARTITION_TOPIC_SUFFIX = "-repartition"
}

class ScalaKStream[K, V](topology: KStreamBuilder, name: String, sourceNodes: Set[String], repartitionRequired: Boolean, dsl: ScalaKStreamBuilder)
    extends KStreamImpl[K, V](topology, name, sourceNodes.asJava, repartitionRequired) {

  import ScalaKStream._

  def filter(p: (K, V) => Boolean): ScalaKStream[K, V] = {
    val name = topology.newName(FILTER_NAME)
    topology.addProcessor(name, new KStreamFilter[K, V](new Predicate[K, V] {
      override def test(key: K, value: V): Boolean = p(key, value)
    }, false), this.name)
    new ScalaKStream[K, V](topology, name, sourceNodes, repartitionRequired, dsl)
  }

  def filterNot(p: (K, V) => Boolean): ScalaKStream[K, V] = {
    val name = topology.newName(FILTER_NAME)
    topology.addProcessor(name, new KStreamFilter[K, V](new Predicate[K, V] {
      override def test(key: K, value: V): Boolean = p(key, value)
    }, true), this.name)
    new ScalaKStream[K, V](topology, name, sourceNodes, repartitionRequired, dsl)
  }

  //  def selectKey[K1](f: (K, V) => K1): KStream[K1, V] = super.selectKey(new KeyValueMapper[K, V, K1] {
  //    override def apply(key: K, value: V): K1 = f(key, value)
  //  })

  //  def mapKeyAndValue[K1, V1](f: (K, V) => (K1, V1)): KStream[K1, V1] = super.map(new KeyValueMapper[K, V, KeyValue[K1, V1]] {
  //    override def apply(key: K, value: V): KeyValue[K1, V1] = {
  //      val tuple = f(key, value)
  //      new KeyValue(tuple._1, tuple._2)
  //    }
  //  })
  //
  //  def mapKeyAndValueAsync[K1, V1](f: (K, V) => Future[(K1, V1)])(implicit ec: ExecutionContext, duration: Duration = 60.seconds): KStream[K1, V1] = super.map(new KeyValueMapper[K, V, KeyValue[K1, V1]] {
  //    override def apply(key: K, value: V): KeyValue[K1, V1] = {
  //      Await.result(f(key, value).map { tuple =>
  //        new KeyValue(tuple._1, tuple._2)
  //      }, duration)
  //    }
  //  })

  def map[V1](f: V => V1): ScalaKStream[K, V1] = {
    val name = topology.newName(MAPVALUES_NAME)
    topology.addProcessor(name, new KStreamMapValues[K, V, V1](new ValueMapper[V, V1] {
      override def apply(value: V): V1 = f(value)
    }), this.name)
    new ScalaKStream[K, V1](topology, name, sourceNodes, repartitionRequired, dsl)
  }

  def mapAsync[V1](f: V => Future[V1])(implicit ec: ExecutionContext, duration: Duration = 60.seconds): ScalaKStream[K, V1] =
    map(value => Await.result(f(value), duration))

  def parseFromAvro[V1](implicit recordFormat: RecordFormat[V1]): ScalaKStream[K, V1] = map {
    case v: GenericRecord => recordFormat.from(v)
    case _                => throw new IllegalArgumentException("Element in stream is not of type GenericRecord")
  }

  def parseFromJson[V1](implicit format: Format[V1]): ScalaKStream[K, V1] = map {
    case v: String => Json.parse(v).as[V1]
    case _         => throw new IllegalArgumentException("Element in stream is not of type String")
  }

  def mapToAvro(implicit recordFormat: RecordFormat[V]): ScalaKStream[K, GenericRecord] =
    map(recordFormat.to)

  def mapToJson(implicit format: Format[V]): ScalaKStream[K, String] =
    map(value => Json.toJson(value).toString)

  def foreach(f: (K, V) => Unit): ScalaKStreamBuilder = {
    super.foreach(new ForeachAction[K, V] {
      override def apply(key: K, value: V): Unit = f(key, value)
    })
    dsl
  }

  def toTopic(topic: String): ScalaKStreamBuilder = {
    super.to(topic)
    dsl
  }
}
