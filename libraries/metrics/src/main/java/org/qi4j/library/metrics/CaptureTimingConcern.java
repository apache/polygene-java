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
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.metrics.MetricsProvider;
import org.qi4j.api.metrics.MetricsTimerFactory;

public class CaptureTimingConcern
    implements InvocationHandler
{
    
    public CaptureTimingConcern( @Service @Optional MetricsProvider metrics, 
                                 @This Composite me, @Structure Qi4jSPI api
                                 )
    {
        MetricsTimerFactory factory = metrics.createFactory( MetricsTimerFactory.class );
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
