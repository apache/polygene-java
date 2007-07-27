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

import org.qi4j.api.Composite;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeCastException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.annotation.Modifies;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

public class CompositeServicesModifier
    implements Composite
{
    @Dependency private CompositeModelFactory modelFactory;
    @Dependency private CompositeBuilderFactory builderFactory;
    @Uses private Composite meAsComposite;
    @Modifies Composite next; //ignore

    public <T extends Composite> T cast( Class<T> compositeType )
    {
        if( isInstance( compositeType ) )
        {
            return compositeType.cast( meAsComposite );
        }
        CompositeModel model = getCompositeModel();
        Class existingCompositeClass = model.getCompositeClass();
        if( !existingCompositeClass.isAssignableFrom( compositeType ) )
        {
            throw new CompositeCastException( existingCompositeClass.getName() + " is not a super-type of " + compositeType.getName() );
        }

        InvocationHandler handler = Proxy.getInvocationHandler( meAsComposite );
        handler = Proxy.getInvocationHandler( ( (ProxyReferenceInvocationHandler) handler ).getProxy() );
        T newComposite = builderFactory.newCompositeBuilder( compositeType ).newInstance();
        Map<Class, Object> oldMixins = ( (RegularCompositeInvocationHandler) handler ).getMixins();
        RegularCompositeInvocationHandler newHandler = RegularCompositeInvocationHandler.getInvocationHandler( newComposite );
        newHandler.setMixins( oldMixins, true );
        return newComposite;
    }

    public boolean isInstance( Class anObjectType )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsComposite );
        Object anObject = ( (ProxyReferenceInvocationHandler) handler ).getProxy();
        if( anObjectType.isInstance( anObject ) )
        {
            return true;
        }
        handler = Proxy.getInvocationHandler( anObject );
        CompositeInvocationHandler oih = (CompositeInvocationHandler) handler;
        return oih.getContext().getCompositeModel().isAssignableFrom( anObjectType );
    }

    public CompositeModel getCompositeModel()
    {
        return modelFactory.getCompositeModel( meAsComposite );
    }
}
