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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.AbstractCompositeInstance;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.spi.composite.CompositeModel;

public final class CompositeMixin
    implements Composite
{
    @ThisCompositeAs private Composite meAsComposite;

    public CompositeModel getCompositeModel()
    {
        Composite composite = dereference();
        return CompositeInstance.getCompositeInstance( composite ).getContext().getCompositeModel();
    }

    public Composite dereference()
    {
        InvocationHandler handler = Proxy.getInvocationHandler( meAsComposite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (Composite) ( (ProxyReferenceInvocationHandler) handler ).getComposite();
        }
        if( handler instanceof AbstractCompositeInstance )
        {
            return meAsComposite;
        }
        return null;
    }
}
