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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeCastException;
import org.qi4j.api.annotation.scope.Modifies;
import org.qi4j.api.annotation.scope.Qi4j;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.api.model.CompositeModel;

public final class CompositeServicesModifier
    implements Composite
{
    @Qi4j private CompositeBuilderFactory builderFactory;
    @ThisAs private Composite meAsComposite;
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
        handler = Proxy.getInvocationHandler( ( (ProxyReferenceInvocationHandler) handler ).getComposite() );
        T newComposite = builderFactory.newCompositeBuilder( compositeType ).newInstance();
        Object[] oldMixins = ( (CompositeInvocationHandler) handler ).getMixins();
        CompositeInvocationHandler newHandler = CompositeInvocationHandler.getInvocationHandler( newComposite );
        newHandler.setMixins( oldMixins );
        return newComposite;
    }

    public boolean isInstance( Class anObjectType )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsComposite );
        Object anObject = ( (ProxyReferenceInvocationHandler) handler ).getComposite();
        if( anObjectType.isInstance( anObject ) )
        {
            return true;
        }
        handler = Proxy.getInvocationHandler( anObject );
        AbstractCompositeInvocationHandler oih = (AbstractCompositeInvocationHandler) handler;
        return oih.getContext().getCompositeModel().getCompositeClass().isAssignableFrom( anObjectType );
    }

    public CompositeModel getCompositeModel()
    {
        Composite composite = dereference();
        return CompositeInvocationHandler.getInvocationHandler( composite ).getContext().getCompositeModel();
    }

    public Composite dereference()
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsComposite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return ( (ProxyReferenceInvocationHandler) handler ).getComposite();
        }
        if( handler instanceof AbstractCompositeInvocationHandler )
        {
            return meAsComposite;
        }

        return null;
    }
}
