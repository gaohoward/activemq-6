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
package org.apache.activemq.core.journal.impl.dataformat;

import org.apache.activemq.api.core.ActiveMQBuffer;
import org.apache.activemq.core.journal.EncodingSupport;

public abstract class JournalInternalRecord implements EncodingSupport
{

   protected int fileID;

   protected byte compactCount;

   public int getFileID()
   {
      return fileID;
   }

   public void setFileID(final int fileID)
   {
      this.fileID = fileID;
   }

   public void decode(final ActiveMQBuffer buffer)
   {
   }

   public void setNumberOfRecords(final int records)
   {
   }

   public int getNumberOfRecords()
   {
      return 0;
   }

   public short getCompactCount()
   {
      return compactCount;
   }

   public void setCompactCount(final short compactCount)
   {
      if (compactCount > Byte.MAX_VALUE)
      {
         this.compactCount = Byte.MAX_VALUE;
      }
      else
      {
         this.compactCount = (byte)compactCount;
      }
   }

   public abstract int getEncodeSize();
}