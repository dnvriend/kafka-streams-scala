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

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.KStreamBuilder

object ScalaKStreamBuilder {
  def apply(config: java.util.Properties): ScalaKStreamBuilder = new ScalaKStreamBuilder(new KStreamBuilder, config)
}

class ScalaKStreamBuilder(topology: KStreamBuilder, config: java.util.Properties) {
  def stream[K, V](topics: String*): ScalaKStream[K, V] = {
    val name: String = topology.newName(KStreamImpl.SOURCE_NAME)
    topology.addSource(name, null, null, topics: _*)
    new ScalaKStream[K, V](topology, name, Set(name), false, this)
  }

  def start(): Unit =
    new KafkaStreams(topology, config).start()
}
