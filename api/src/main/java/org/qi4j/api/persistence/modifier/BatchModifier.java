/*
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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

package org.qi4j.api.persistence.modifier;

import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.persistence.binding.PersistenceBinding;
import org.qi4j.api.InvocationContext;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@AppliesTo( Serializable.class )
public final class BatchModifier
    implements InvocationHandler
{
    @Uses private PersistenceBinding persistent;
    @Dependency private InvocationContext context;
    @Modifies private Object next;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
