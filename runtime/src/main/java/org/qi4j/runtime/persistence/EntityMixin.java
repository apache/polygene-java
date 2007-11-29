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
package org.qi4j.runtime.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import org.qi4j.Composite;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.annotation.scope.Structure;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.entity.CompositeCastException;
import org.qi4j.entity.Entity;
import org.qi4j.entity.EntityComposite;
import org.qi4j.runtime.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.AbstractCompositeInstance;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.spi.composite.CompositeModel;

public final class EntityMixin
    implements Entity
{
    @Structure private CompositeBuilderFactory builderFactory;
    @ThisCompositeAs private EntityComposite meAsEntity;

    public <T extends Composite> T cast( Class<T> compositeType )
    {
        if( compositeType.isInstance( compositeType ) )
        {
            return compositeType.cast( meAsEntity );
        }
        CompositeInstance handler = CompositeInstance.getCompositeInstance( meAsEntity );
        CompositeModel model = handler.getContext().getCompositeModel();
        Class existingCompositeClass = model.getCompositeClass();
        if( !existingCompositeClass.isAssignableFrom( compositeType ) )
        {
            throw new CompositeCastException( existingCompositeClass.getName() + " is not a super-type of " + compositeType.getName() );
        }


        T newComposite = builderFactory.newCompositeBuilder( compositeType ).newInstance();
        Object[] oldMixins = handler.getMixins();
        CompositeInstance newHandler = CompositeInstance.getCompositeInstance( newComposite );

        newHandler.setMixins( oldMixins );
        return newComposite;
    }

    public boolean isInstance( Class anObjectType )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsEntity );
        Object anObject = ( (ProxyReferenceInvocationHandler) handler ).getComposite();
        if( anObjectType.isInstance( anObject ) )
        {
            return true;
        }
        handler = Proxy.getInvocationHandler( anObject );
        AbstractCompositeInstance oih = (AbstractCompositeInstance) handler;
        return oih.getContext().getCompositeModel().getCompositeClass().isAssignableFrom( anObjectType );
    }

    public boolean isReference()
    {
        EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( meAsEntity );
        return handler.isReference();
    }

    public URL toURL()
    {
        return null;  //toDo()
    }
}
