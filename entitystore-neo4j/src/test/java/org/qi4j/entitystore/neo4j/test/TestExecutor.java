/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j.test;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;

public abstract class TestExecutor
{
    protected abstract void setup() throws Exception;

    protected abstract void verify() throws Exception;

    static protected void setup(UnitOfWork uow, TestExecutor executor) throws Exception
    {
        executor.uow = uow;
        try
        {
            executor.setup();
        }
        finally
        {
            executor.uow = null;
        }
    }

    static protected void verify(UnitOfWork uow, TestExecutor executor) throws Exception
    {
        executor.uow = uow;
        try
        {
            executor.verify();
        }
        finally
        {
            executor.uow = null;
        }
    }

    private UnitOfWork uow = null;

    protected TestExecutor()
    {
    }

    protected <T extends EntityComposite> T find(String identity, Class<T> compositeType)
    {
        return uow.get(compositeType, identity);
    }

    protected <T extends EntityComposite> T newEntity(Class<T> compositeType)
    {
        return uow.newEntity(compositeType);
    }

    protected <T extends EntityComposite> T newEntity(String identity, Class<T> compositeType)
    {
        return uow.newEntity(compositeType, identity);
    }

    protected <T extends EntityComposite> EntityBuilder<T> newEntityBuilder(Class<T> compositeType)
    {
        return uow.newEntityBuilder(compositeType);
    }

    protected <T extends EntityComposite> EntityBuilder<T> newEntityBuilder(String identity, Class<T> compositeType)
    {
        return uow.newEntityBuilder(compositeType, identity);
    }
}
