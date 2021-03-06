/*
 * Copyright 2014 Michael Krolikowski
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
package com.github.mkroli.dns4s.section.resource

import java.net.Inet6Address
import java.net.InetAddress

import com.github.mkroli.dns4s.MessageBuffer
import com.github.mkroli.dns4s.section.Resource

case class AAAAResource(address: Inet6Address) extends Resource {
  def apply(buf: MessageBuffer) = buf.put(address.getAddress())
}

object AAAAResource {
  def apply(buf: MessageBuffer) = {
    InetAddress.getByAddress(buf.getBytes(16).toArray) match {
      case addr: Inet6Address => new AAAAResource(addr)
    }
  }
}
