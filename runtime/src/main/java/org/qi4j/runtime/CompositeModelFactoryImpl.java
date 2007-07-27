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
package org.qi4j.runtime;

import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.Composite;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.runtime.CompositeModelImpl;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompositeModelFactoryImpl
    implements CompositeModelFactory
{
    private Map<Class, CompositeModelImpl> composites;

    public CompositeModelFactoryImpl()
    {
        composites = new ConcurrentHashMap<Class, CompositeModelImpl>();
    }

    public <T> T dereference( T proxy )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( proxy );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (T) ( (ProxyReferenceInvocationHandler) handler ).getProxy();
        }
        if( handler instanceof CompositeInvocationHandler )
        {
            return proxy;
        }

        return null;
    }


    public CompositeModel getCompositeModel( Class compositeType )
    {
        if( compositeType == null )
        {
            throw new NullArgumentException( "compositeType" );
        }
        CompositeModelImpl compositeModel = composites.get( compositeType );
        if( compositeModel == null )
        {
            compositeModel = new CompositeModelImpl( compositeType );
            composites.put( compositeType, compositeModel );
        }

        return compositeModel;
    }

    public CompositeModel getCompositeModel( Composite aComposite )
    {
        aComposite = dereference( aComposite );
        return RegularCompositeInvocationHandler.getInvocationHandler( aComposite ).getContext().getCompositeModel();
    }

}
