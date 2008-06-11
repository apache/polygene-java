/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConcernOf;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;


public abstract class AbstractTraceConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @Structure Qi4j api;
    @Service protected LogService logService;
    private Composite thisComposite;
    private Class compositeType;

    public AbstractTraceConcern( Composite thisComposite )
    {
        this.thisComposite = thisComposite;
        compositeType = thisComposite.getClass().getInterfaces()[ 0 ];
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        boolean doTrace = doTrace2();
        Object result;
        try
        {
            if( doTrace )
            {
                logService.traceEntry( compositeType, api.dereference( thisComposite ), method, args );
            }
            result = next.invoke( proxy, method, args );
            if( doTrace )
            {
                logService.traceExit( compositeType, api.dereference( thisComposite ), method, args, result );
            }
        }
        catch( Throwable t )
        {
            if( doTrace )
            {
                logService.traceException( compositeType, api.dereference( thisComposite ), method, args, t );
            }
            throw t;
        }
        return result;
    }

    protected boolean doTrace2()
    {
        return true;
    }
}
