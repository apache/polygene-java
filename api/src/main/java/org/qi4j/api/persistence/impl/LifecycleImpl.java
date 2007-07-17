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
    private static int STATE_DIRTY = 1;
    private static int STATE_DELETED = 2;
    private static int STATE_NEW = 4;
    private static int STATE_TRANSACTIONAL = 8;
    private static int STATE_DETACHED = 16;

    @Uses private EntityComposite meAsEntity;
    private int state;

    public LifecycleImpl()
    {
        state = STATE_NEW;
    }

    public void create()
        throws PersistenceException
    {
        // TODO
        state = state & ~STATE_NEW;
    }

    public void initialize()
        throws PersistenceException
    {
        // TODO:
    }

    public void delete()
        throws PersistenceException
    {
        // TODO
        state = state | STATE_DELETED;
    }

    public boolean isDirty()
    {
        return (state & STATE_DIRTY) == STATE_DIRTY;
    }

    public boolean isNew()
    {
        return (state & STATE_NEW) == STATE_NEW;
    }

    public boolean isTransactional()
    {
        return (state & STATE_TRANSACTIONAL) == STATE_TRANSACTIONAL;
    }

    public boolean isDeleted()
    {
        return (state & STATE_DELETED) == STATE_DELETED;
    }

    public boolean isDetached()
    {
        return (state & STATE_DETACHED) == STATE_DETACHED;
    }

    public void detach()
    {
        state = state | STATE_DETACHED;
    }

    public void makeDirty()
    {
        state = state | STATE_DIRTY;
    }

    public void setTransactional( boolean transactional )
    {
        if( transactional )
        {
            state = state | STATE_TRANSACTIONAL;
        }
        else
        {
            state = state & ~STATE_TRANSACTIONAL;
        }
    }
}
