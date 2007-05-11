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
package org.qi4j.api.persistence.modifier;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.ObjectNotFoundException;
import org.qi4j.api.persistence.PersistentRepository;
import org.qi4j.api.persistence.binding.PersistenceBinding;

/**
 * This modifier ensures that objects have a proper reference
 * to its repository.
 */
public final class PersistentRepositoryReferenceModifier
    implements PersistentRepository
{
    @Uses
    PersistentRepository repo;
    @Modifies
    PersistentRepository repository;

    public void create( PersistenceBinding aProxy )
    {
        repository.create( aProxy );
        aProxy.setPersistentRepository( repo );
    }

    public void read( PersistenceBinding aProxy ) throws ObjectNotFoundException
    {
        repository.read( aProxy );
        aProxy.setPersistentRepository( repo );
    }

    public void update( PersistenceBinding aProxy, Object aMixin )
    {
        repository.update( aProxy, aMixin );
    }

    public void delete( PersistenceBinding aProxy )
    {
        repository.delete( aProxy );
        aProxy.setPersistentRepository( null );
    }
}
