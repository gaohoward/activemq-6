/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.core.server.cluster;

import org.apache.activemq.core.protocol.ServerPacketDecoder;
import org.apache.activemq.core.protocol.core.impl.ActiveMQClientProtocolManager;
import org.apache.activemq.core.protocol.core.impl.PacketDecoder;
import org.apache.activemq.spi.core.remoting.ClientProtocolManager;
import org.apache.activemq.spi.core.remoting.ClientProtocolManagerFactory;

/**
 * A protocol manager that will replace the packet manager for inter-server communications
 */
public class ActiveMQServerSideProtocolManagerFactory implements ClientProtocolManagerFactory
{
   private static final ActiveMQServerSideProtocolManagerFactory INSTANCE = new ActiveMQServerSideProtocolManagerFactory();

   public static ActiveMQServerSideProtocolManagerFactory getInstance()
   {
      return INSTANCE;
   }

   private ActiveMQServerSideProtocolManagerFactory()
   {
   }

   private static final long serialVersionUID = 1;

   @Override
   public ClientProtocolManager newProtocolManager()
   {
      return new ActiveMQReplicationProtocolManager();
   }

   class ActiveMQReplicationProtocolManager extends ActiveMQClientProtocolManager
   {
      @Override
      protected PacketDecoder getPacketDecoder()
      {
         return ServerPacketDecoder.INSTANCE;
      }
   }
}