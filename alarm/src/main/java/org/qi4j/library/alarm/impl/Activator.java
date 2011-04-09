/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.alarm.impl;

//import org.osgi.framework.BundleActivator;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceRegistration;

public class Activator
//    implements BundleActivator
{
    private AlarmServiceImpl m_AlarmService;
//    private ServiceRegistration m_ServiceRegistration;
    private ProviderTracker m_tracker;

//    public void start( BundleContext bundleContext )
//        throws Exception
//    {
//        m_AlarmService = new AlarmServiceImpl();
//        String service = AlarmService.class.getName();
//        m_ServiceRegistration = bundleContext.registerService( service, m_AlarmService, null );
//        m_tracker = new ProviderTracker( bundleContext, m_AlarmService );
//        m_tracker.open();
//    }

//    public void stop( BundleContext bundleContext )
//        throws Exception
//    {
//        m_tracker.close();
//        m_ServiceRegistration.unregister();
//        List listeners = m_AlarmService.getAlarmListeners();
//        Iterator list = listeners.iterator();
//        while( list.hasNext() )
//        {
//            AlarmListener listener = (AlarmListener) list.next();
//            m_AlarmService.removeAlarmListener( listener );
//        }
//        AlarmModel[] models = m_AlarmService.getAlarmModels();
//        for( int i = 0; i < models.length; i++ )
//        {
//            m_AlarmService.removeAlarmModel( models[ i ] );
//        }
//    }
}
