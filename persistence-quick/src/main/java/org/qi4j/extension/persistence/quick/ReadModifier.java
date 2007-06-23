/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.extension.persistence.quick;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.composite.PersistentComposite;
import org.qi4j.runtime.CompositeInvocationHandler;

/**
 * When methods in stateful mixins that modify state have been called
 * this modifier will store it in a repository, if one has been set.
 */
@AppliesTo( Serializable.class )
public final class ReadModifier
    implements InvocationHandler
{
    @Uses PersistentComposite persistent;
    @Dependency CompositeInvocationHandler handler;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Object result = method.invoke( proxy, args );

        // Store mixin
        PersistentStorage storage = persistent.getPersistentStorage();
        if( storage != null && !method.getName().startsWith( "get" ) )
        {
            Object object = CompositeInvocationHandler.getInvocationHandler( proxy );
            storage.update( persistent, (Serializable) object );
        }

        return result;
    }
}
