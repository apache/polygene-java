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
package org.qi4j.api.persistence.impl;

import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.composite.EntityComposite;


public final class LifecycleImpl
    implements Lifecycle
{
    public void create()
        throws PersistenceException
    {
    }

    public void delete()
        throws PersistenceException
    {
    }
}
