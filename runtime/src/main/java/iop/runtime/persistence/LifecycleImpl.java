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
package iop.runtime.persistence;

import iop.api.persistence.Lifecycle;
import iop.api.persistence.PersistenceException;
import iop.api.persistence.PersistentRepository;
import iop.api.persistence.binding.PersistenceBinding;
import iop.api.annotation.Uses;

public final class LifecycleImpl
    implements Lifecycle
{
    @Uses PersistenceBinding meAsPersistence;

    public void create() throws PersistenceException
    {
        PersistentRepository repository = meAsPersistence.getPersistentRepository();
        if (repository == null)
            throw new PersistenceException( "No repository set for object");
        repository.create( meAsPersistence);
    }

    public void delete() throws PersistenceException
    {
        PersistentRepository repository = meAsPersistence.getPersistentRepository();
        if (repository == null)
            throw new PersistenceException( "No repository set for object");
        repository.delete( meAsPersistence);
    }
}
