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

package org.apache.polygene.library.logging.trace;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.library.logging.trace.service.TraceService;


public abstract class AbstractTraceConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @Structure private PolygeneAPI api;
    @Optional @Service protected TraceService traceService;
    private Composite thisComposite;
    private Class compositeType;

    public AbstractTraceConcern( Composite thisComposite )
    {
        this.thisComposite = thisComposite;
        compositeType = thisComposite.getClass().getInterfaces()[ 0 ];
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        boolean doTrace = traceService != null && doTrace();
        Object result;
        Instant entryTime = SystemTime.now();;
        try
        {
            result = next.invoke( proxy, method, args );
            if( doTrace )
            {
                Duration duration = Duration.between(entryTime, SystemTime.now() );
                traceService.traceSuccess( compositeType, api.dereference( thisComposite ), method, args, result, entryTime, duration );
            }
        }
        catch( Throwable t )
        {
            if( doTrace )
            {
                Duration duration = Duration.between(entryTime, SystemTime.now() );
                Composite object = api.dereference( thisComposite );
                traceService.traceException( compositeType, object, method, args, t, entryTime, duration );
            }
            throw t;
        }
        return result;
    }

    protected boolean doTrace()
    {
        return true;
    }
}
