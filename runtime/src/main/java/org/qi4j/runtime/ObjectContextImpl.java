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
package org.qi4j.runtime;

import org.qi4j.api.ObjectFactory;
import org.qi4j.api.MixinFactory;
import org.qi4j.spi.object.ObjectContext;
import org.qi4j.spi.object.InvocationInstance;
import org.qi4j.spi.object.InvocationInstancePool;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;

/**
 * TODO
 *
 */
public final class ObjectContextImpl
    implements ObjectContext
{
    private Class bindingType;
    private ObjectFactory objectFactory;
    private MixinFactory mixinFactory;
    private InvocationInstancePool pool;
    private Map<Method, List<InvocationInstance>> methodToInvocationInstanceMap;

    public ObjectContextImpl( Class aBindingType, ObjectFactory aObjectFactory, MixinFactory aMixinFactory, InvocationInstancePool instancePool)
    {
        bindingType = aBindingType;
        objectFactory = aObjectFactory;
        mixinFactory = aMixinFactory;
        pool = instancePool;
        methodToInvocationInstanceMap = instancePool.getPool( aBindingType);
    }

    public Class getBindingType()
    {
        return bindingType;
    }

    public ObjectFactory getObjectFactory()
    {
        return objectFactory;
    }

    public MixinFactory getMixinFactory()
    {
        return mixinFactory;
    }

    public InvocationInstancePool getPool()
    {
        return pool;
    }

    public InvocationInstance newInvocationInstance( Method method, Object mixin, List<InvocationInstance> instances )
    {
        return pool.newInstance( method, bindingType, mixin, instances );
    }

    public Map<Method, List<InvocationInstance>> getMethodToInvocationInstanceMap()
    {
        return methodToInvocationInstanceMap;
    }
}
