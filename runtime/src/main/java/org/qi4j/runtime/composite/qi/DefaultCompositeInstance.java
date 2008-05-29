/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * InvocationHandler for proxy objects.
 */
public final class DefaultCompositeInstance
    implements CompositeInstance
{
    private Object proxy;
    private Object[] mixins;
    private CompositeModel composite;
    private ModuleInstance moduleInstance;

    public DefaultCompositeInstance( CompositeModel composite, ModuleInstance moduleInstance, Object[] mixins )
    {
        this.composite = composite;
        this.moduleInstance = moduleInstance;
        this.mixins = mixins;

        proxy = composite.newProxy( this );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return composite.invoke( mixins, proxy, method, args, moduleInstance );
    }

    public Object proxy()
    {
        return proxy;
    }

    public CompositeModel composite()
    {
        return composite;
    }

    public ModuleInstance moduleInstance()
    {
        return moduleInstance;
    }

    public void setMixins( Object[] newMixins )
    {
        this.mixins = newMixins;
    }

    public Object[] getMixins()
    {
        return mixins;
    }

    public String toURI()
    {
        return ""; // "urn:qi4j:composite:" + context.getCompositeModel().getCompositeType().getName();
    }
}