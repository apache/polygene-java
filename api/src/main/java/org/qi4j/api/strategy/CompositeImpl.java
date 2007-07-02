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
package org.qi4j.api.strategy;

import org.qi4j.api.Composite;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeModel;

public final class CompositeImpl
    implements Composite
{
    @Dependency private CompositeFactory delegate;
    @Uses private Composite meAsComposite;

    public <T extends Composite> T cast( Class<T> anObjectType )
    {
        return delegate.cast( anObjectType, delegate.dereference( meAsComposite ) );
    }

    public boolean isInstance( Class anObjectType )
    {
        return delegate.isInstance( anObjectType, delegate.dereference( meAsComposite ) );
    }

    public CompositeModel getCompositeModel()
    {
        return delegate.getCompositeModel( meAsComposite );
    }
}
