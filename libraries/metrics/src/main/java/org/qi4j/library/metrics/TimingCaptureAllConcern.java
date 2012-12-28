/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.metrics;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.metrics.MetricsTimer;
import org.qi4j.api.metrics.MetricsTimerFactory;

public class TimingCaptureAllConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private MetricsTimer timer;

    public TimingCaptureAllConcern( @Service @Optional MetricsProvider metrics,
                                    @Invocation Method method
    )
    {
        if( metrics != null )
        {
            MetricsTimerFactory factory = metrics.createFactory( MetricsTimerFactory.class );
            boolean annotated = method.getAnnotation( TimingCapture.class ) != null;
            String captureNme = getMethodName( method ) + "() ["  +( annotated ? "@" : "" ) + "TimingCapture" + "]";
            Class<?> declaringClass = method.getDeclaringClass();
            timer = factory.createTimer( declaringClass, captureNme, TimeUnit.MILLISECONDS, TimeUnit.SECONDS );
        }
    }

    private String getMethodName( Method method )
    {
        return method.getName();
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
