/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.entity.property;

import org.qi4j.entity.property.PropertyChange;
import org.qi4j.entity.property.PropertyChangeObserver;

/**
 * TODO
 */
public class ChainedChangeObserver<T> implements PropertyChangeObserver<T>
{
    private PropertyChangeObserver<T> changeObserver;
    private PropertyChangeObserver<T> changeObserver1;

    public ChainedChangeObserver( PropertyChangeObserver<T> changeObserver, PropertyChangeObserver<T> changeObserver1 )
    {
        this.changeObserver = changeObserver;
        this.changeObserver1 = changeObserver1;
    }

    public void onChange( PropertyChange<T> change )
    {
        changeObserver.onChange( change );
        changeObserver1.onChange( change );
    }
}
