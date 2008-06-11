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
package org.qi4j.runtime.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.Entity;
import org.qi4j.entity.EntityCastException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;

public final class EntityMixin
    implements Entity
{
    @Structure private CompositeBuilderFactory builderFactory;
    @This private EntityComposite meAsEntity;

    // TODO This needs to use UnitOfWork.newEntityBuilder to be correct
    public <T extends Composite> T cast( Class<T> compositeType )
    {
        if( compositeType.isInstance( compositeType ) )
        {
            return compositeType.cast( meAsEntity );
        }
        EntityInstance entityInstance = EntityInstance.getEntityInstance( meAsEntity );
        Class existingCompositeClass = entityInstance.type();
        if( !existingCompositeClass.isAssignableFrom( compositeType ) )
        {
            throw new EntityCastException( existingCompositeClass.getName() + " is not a super-type of " + compositeType.getName() );
        }

        T newComposite = builderFactory.newCompositeBuilder( compositeType ).newInstance();
        EntityInstance newEntityInstance = EntityInstance.getEntityInstance( newComposite );

        entityInstance.cast( newEntityInstance );

        return newComposite;
    }

    public boolean isInstance( Class anObjectType )
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsEntity );
        Object anObject = ( (ProxyReferenceInvocationHandler) handler ).proxy();
        if( anObjectType.isInstance( anObject ) )
        {
            return true;
        }
        handler = Proxy.getInvocationHandler( anObject );
        EntityInstance entityInstance = (EntityInstance) handler;
        return entityInstance.type().isAssignableFrom( anObjectType );
    }

    public boolean isReference()
    {
        EntityInstance handler = EntityInstance.getEntityInstance( meAsEntity );
        return handler.isReference();
    }
}
