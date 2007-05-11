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
package iop.runtime;

import iop.api.ObjectFactory;
import iop.api.ObjectHelper;
import iop.api.ObjectStrategy;
import iop.api.annotation.Dependency;
import iop.api.annotation.Uses;

public final class ObjectStrategyDelegatorImpl
    implements ObjectStrategy
{
    @Dependency ObjectFactory delegate;
    @Uses ObjectStrategy meAsStrategy;

    public <T> T newInstance( Class<T> anObjectType )
    {
        return delegate.newInstance( anObjectType );
    }

    public <T> T cast( Class<T> anObjectType )
    {
        return delegate.cast( anObjectType, ObjectHelper.getThat( meAsStrategy ) );
    }

    public boolean isInstance( Class anObjectType )
    {
        return delegate.isInstance( anObjectType, ObjectHelper.getThat( meAsStrategy ) );
    }


}
