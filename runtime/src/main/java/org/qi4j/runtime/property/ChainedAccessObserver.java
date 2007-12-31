/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.property;

import org.qi4j.property.PropertyAccess;
import org.qi4j.property.PropertyAccessObserver;

/**
 * TODO
 */
public final class ChainedAccessObserver<T>
    implements PropertyAccessObserver<T>
{
    private PropertyAccessObserver<T> accessObserver;
    private PropertyAccessObserver<T> accessObserver1;

    public ChainedAccessObserver( PropertyAccessObserver<T> accessObserver, PropertyAccessObserver<T> accessObserver1 )
    {
        this.accessObserver = accessObserver;
        this.accessObserver1 = accessObserver1;
    }

    public void onAccess( PropertyAccess<T> access )
    {
        accessObserver.onAccess( access );
        accessObserver1.onAccess( access );
    }
}
