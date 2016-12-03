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

package org.apache.zest.library.metrics;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.Invocation;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.metrics.MetricNames;
import org.apache.zest.api.metrics.MetricsProvider;
import org.apache.zest.api.metrics.MetricsTimer;
import org.apache.zest.api.metrics.MetricsTimerFactory;
import org.apache.zest.api.structure.Module;

public class TimingCaptureAllConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private final MetricsTimer timer;

    public TimingCaptureAllConcern( @Structure Module module,
                                    @Service @Optional MetricsProvider metrics,
                                    @Invocation Method method
    )
    {
        if( metrics == null )
        {
            timer = null;
        }
        else
        {
            MetricsTimerFactory factory = metrics.createFactory( MetricsTimerFactory.class );
            TimingCapture capture = method.getAnnotation( TimingCapture.class );
            String timerName;
            if( capture == null || "".equals( capture.value() ) )
            {
                timerName = MetricNames.nameFor( module, method );
            }
            else
            {
                timerName = capture.value();
            }
            timer = factory.createTimer( timerName );
        }
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        MetricsTimer.Context timing = null;
        if( timer != null )
        {
            timing = timer.start();
        }
        try
        {
            return next.invoke( proxy, method, args );
        }
        finally
        {
            if( timing != null )
            {
                timing.stop();
            }
        }
    }
}
