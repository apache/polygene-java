package org.qi4j.library.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.*;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public class MBeanTracker
{
    private boolean started = false;
    private ObjectName pattern;
    private MBeanServer server;
    private List<TrackerCallback> callbacks = new ArrayList<TrackerCallback>();
    private List<ObjectName> tracked = Collections.synchronizedList( new ArrayList<ObjectName>() );
    private RegistrationListener registrationListener;

    public MBeanTracker(ObjectName pattern, MBeanServer server)
    {
       this.pattern = pattern;
       this.server = server;
    }

    public MBeanTracker registerCallback(TrackerCallback callback)
    {
       List<TrackerCallback> newList = new ArrayList<TrackerCallback>(callbacks);
       newList.add(callback);
       callbacks = newList;
       return this;
    }

    public MBeanTracker unregisterCallback(TrackerCallback callback)
    {
       List<TrackerCallback> newList = new ArrayList<TrackerCallback>(callbacks);
       newList.remove(callback);
       callbacks = newList;
       return this;
    }

    public Iterable<ObjectName> getTracked()
    {
       return tracked;
    }

    public void start()
    {
       if (!started)
       {
          try
          {
             registrationListener = new RegistrationListener();
             server.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), registrationListener, null, null);

             for (ObjectName objectName : server.queryNames(pattern, null))
             {
                for (TrackerCallback callback : callbacks)
                {
                   try
                   {
                      tracked.add(objectName);
                      callback.addedMBean(objectName, server);
                   } catch (Throwable throwable)
                   {
                      LoggerFactory.getLogger( MBeanTracker.class ).error("Tracker callback threw exception", throwable);
                   }
                }
             }

             started = true;
          } catch (Exception e)
          {
             // Could not start
          }
       }
    }

    public void stop()
    {
       if (started)
       {
          try
          {
             server.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), registrationListener);

             for (ObjectName objectName : server.queryNames(pattern, null))
             {
                for (TrackerCallback callback : callbacks)
                {
                   try
                   {
                      callback.removedMBean(objectName, server);
                      tracked.remove(objectName);
                   } catch (Throwable throwable)
                   {
                      LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                   }
                }
             }

             started = false;
          } catch (Exception e)
          {
             // Could not start
          }
       }
    }

    /**
     * Callback that is used to notify when MBeans matching the given pattern becomes
     * added or removed.
     */
    public interface TrackerCallback
    {
       void addedMBean(ObjectName newMBean, MBeanServer server)
          throws Throwable;

       void removedMBean(ObjectName removedMBean, MBeanServer server)
          throws Throwable;
    }

    /**
     * Registers the given NotificationListener+NotificationFilter on tracked MBeans.
     */
    public static class NotificationTracker
        implements TrackerCallback
    {
        private NotificationListener listener;
        private NotificationFilter filter;

        public NotificationTracker( NotificationListener listener, NotificationFilter filter )
        {
            this.listener = listener;
            this.filter = filter;
        }

        @Override
        public void addedMBean( ObjectName newMBean, MBeanServer server ) throws Throwable
        {
            server.addNotificationListener( newMBean, listener, filter,  null);
        }

        @Override
        public void removedMBean( ObjectName removedMBean, MBeanServer server ) throws Throwable
        {
            server.removeNotificationListener( removedMBean, listener );
        }
    }

    private class RegistrationListener
       implements NotificationListener
    {
       @Override
       public void handleNotification(Notification notification, Object o)
       {
          if (notification instanceof MBeanServerNotification )
          {
             MBeanServerNotification serverNotification = (MBeanServerNotification) notification;
             if (pattern.apply(serverNotification.getMBeanName()))
             {
                if (serverNotification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
                {
                   for (TrackerCallback callback : callbacks)
                   {
                      try
                      {
                         tracked.add(serverNotification.getMBeanName());
                         callback.addedMBean(serverNotification.getMBeanName(), server);
                      } catch (Throwable throwable)
                      {
                         LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                      }
                   }
                } else if (serverNotification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
                {
                   for (TrackerCallback callback : callbacks)
                   {
                      try
                      {
                         callback.removedMBean(serverNotification.getMBeanName(), server);
                         tracked.remove(serverNotification.getMBeanName());
                      } catch (Throwable throwable)
                      {
                         LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                      }
                   }
                }
             }
          }
       }
    }
}
