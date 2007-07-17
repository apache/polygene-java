/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.test.model;

import java.io.Serializable;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.composite.PersistentStorage;
import org.qi4j.api.EntityRepository;

public final class DummyEntityRepository
    implements EntityRepository
{
    public <T extends EntityComposite> T getInstance( String identity, Class<T> type )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> T getInstance( String identity, Class<T> type, boolean autoCreate )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> T newInstance( String identity, Class<T> type )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <T extends EntityComposite> void create( T t )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
