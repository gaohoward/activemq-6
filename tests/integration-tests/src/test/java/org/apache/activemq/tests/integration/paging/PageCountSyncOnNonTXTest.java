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
package org.apache.activemq.tests.integration.paging;

import org.apache.activemq.api.core.SimpleString;
import org.apache.activemq.api.core.client.ClientConsumer;
import org.apache.activemq.api.core.client.ClientMessage;
import org.apache.activemq.api.core.client.ClientProducer;
import org.apache.activemq.api.core.client.ClientSession;
import org.apache.activemq.api.core.client.ClientSessionFactory;
import org.apache.activemq.api.core.client.ServerLocator;
import org.apache.activemq.core.server.ActiveMQServer;
import org.apache.activemq.core.server.Queue;
import org.apache.activemq.tests.util.RandomUtil;
import org.apache.activemq.tests.util.ServiceTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Clebert Suconic
 */

public class PageCountSyncOnNonTXTest extends ServiceTestBase
{

   // We will add a random factor on the wait time
   private long timeToRun;

   Process process;

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      timeToRun = 30000 + RandomUtil.randomPositiveInt() % 1000;
   }

   @Test
   public void testSendNoTx() throws Exception
   {
      String QUEUE_NAME = "myQueue";

      process = PageCountSyncServer.spawnVM(getTestDir(), timeToRun);

      ServerLocator locator = createNettyNonHALocator();

      try
      {
         locator = createNettyNonHALocator();
         locator.setReconnectAttempts(0);
         locator.setInitialConnectAttempts(10);
         locator.setRetryInterval(500);

         locator.setBlockOnDurableSend(false);

         ClientSessionFactory factory = locator.createSessionFactory();
         ClientSession session = factory.createSession(true, true);
         session.createQueue(QUEUE_NAME, QUEUE_NAME, true);
         ClientProducer producer = session.createProducer(QUEUE_NAME);
         ClientConsumer consumer = session.createConsumer(QUEUE_NAME);
         session.start();


         ClientSession sessionTransacted = factory.createSession(false, false);
         ClientProducer producerTransacted = sessionTransacted.createProducer(QUEUE_NAME);
         ClientConsumer consumerTransacted = sessionTransacted.createConsumer(QUEUE_NAME);
         sessionTransacted.start();


         long start = System.currentTimeMillis();

         long nmsgs = 0;


         try
         {
            while (true)
            {

               int size = RandomUtil.randomPositiveInt() % 1024;

               if (size == 0)
               {
                  size = 1024;
               }
               ClientMessage msg = session.createMessage(true);
               msg.getBodyBuffer().writeBytes(new byte[size]);

               producer.send(msg);

               if (++nmsgs % 100 == 0)
               {
                  // complicating the test a bit with transacted sends and consuming
                  producerTransacted.send(msg);

                  for (int i = 0; i < 50; i++)
                  {
                     msg = consumerTransacted.receive(100);
                     if (msg != null)
                     {
                        msg.acknowledge();
                     }
                  }

                  sessionTransacted.commit();

                  msg = consumer.receive(100);
                  if (msg != null)
                  {
                     msg.acknowledge();
                  }
               }

               if (System.currentTimeMillis() - start > timeToRun)
               {
                  // this will ensure to capture a failure since the server will have crashed
                  session.commit();
               }
            }
         }
         catch (Exception expected)
         {
            expected.printStackTrace();
         }

      }
      finally
      {
         locator.close();
      }
      assertEquals("Process didn't end as expected", 1, process.waitFor());


      ActiveMQServer server = PageCountSyncServer.createServer(getTestDir());

      try
      {
         server.start();

         Thread.sleep(500);

         locator = createNettyNonHALocator();

         try
         {
            Queue queue = server.locateQueue(new SimpleString(QUEUE_NAME));

            assertNotNull(queue);

            long msgs = getMessageCount(queue);

            ClientSessionFactory factory = locator.createSessionFactory();

            ClientSession session = factory.createSession(false, false);

            ClientConsumer consumer = session.createConsumer(QUEUE_NAME, false);

            session.start();

            for (int i = 0; i < msgs; i++)
            {
               ClientMessage msg = consumer.receive(5000);
               assertNotNull(msg);
               //  msg.acknowledge(); -- we don't ack
               // because in case of a failure we can check data with print-data
            }

            assertNull(consumer.receiveImmediate());

            session.close();


         }
         finally
         {
            locator.close();
         }


      }
      finally
      {
         server.stop();
      }

   }


}
