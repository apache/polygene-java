/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.library.circuitbreaker.jmx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.time.Instant;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;

/**
 * MBean for circuit breakers. Exposes CB properties and also the property changes as notifications.
 */
public class CircuitBreakerJMX
        extends NotificationBroadcasterSupport
        implements org.apache.zest.library.circuitbreaker.jmx.CircuitBreakerJMXMBean
{
   CircuitBreaker circuitBreaker;

   public CircuitBreakerJMX(CircuitBreaker circuitBreaker, final ObjectName mbeanObjectName)
   {
      super(new MBeanNotificationInfo(new String[]{"serviceLevel", "status"}, Notification.class.getName(), "Circuit breaker notifications"));

      this.circuitBreaker = circuitBreaker;
      circuitBreaker.addPropertyChangeListener(new PropertyChangeListener()
      {
         long sequenceNr = System.currentTimeMillis();

         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
            Notification notification = new Notification(evt.getPropertyName(), mbeanObjectName, sequenceNr++, System.currentTimeMillis(), evt.getNewValue().toString());
            sendNotification(notification);
         }
      });
   }

   @Override
   public String getStatus()
   {
      return circuitBreaker.status().name();
   }

   @Override
   public int getThreshold()
   {
      return circuitBreaker.threshold();
   }

   @Override
   public double getServiceLevel()
   {
      return circuitBreaker.serviceLevel();
   }

   @Override
   public String getLastErrorMessage()
   {
      return circuitBreaker.lastThrowable() == null ? "" : errorMessage(circuitBreaker.lastThrowable());
   }

   private String errorMessage(Throwable throwable)
   {
      String message = throwable.getMessage();
      if (message == null)
         message = throwable.getClass().getSimpleName();

      if (throwable.getCause() != null)
      {
         return message + ":" + errorMessage(throwable.getCause());
      } else
         return message;
   }

   @Override
   public String getTrippedOn()
   {
      Instant trippedOn = circuitBreaker.trippedOn();
      return trippedOn == null ? "" : trippedOn.toString();
   }

   @Override
   public String getEnableOn()
   {
      Instant trippedOn = circuitBreaker.enabledOn();
      return trippedOn == null ? "" : trippedOn.toString();
   }

   @Override
   public String turnOn()
   {
      try
      {
         circuitBreaker.turnOn();
         return "Circuit breaker has been turned on";
      } catch (PropertyVetoException e)
      {
         return "Could not turn on circuit breaker:" + getLastErrorMessage();
      }
   }

   @Override
   public void trip()
   {
      circuitBreaker.trip();
   }
}
