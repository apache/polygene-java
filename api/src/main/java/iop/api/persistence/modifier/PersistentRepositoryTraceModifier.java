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
package iop.api.persistence.modifier;

import iop.api.annotation.Modifies;
import iop.api.persistence.ObjectNotFoundException;
import iop.api.persistence.PersistentRepository;
import iop.api.persistence.binding.PersistenceBinding;

/**
 * This modifier traces calls to a persistent repository
 */
public final class PersistentRepositoryTraceModifier
    implements PersistentRepository
{
    @Modifies
    PersistentRepository repository;

    public void create( PersistenceBinding aProxy )
    {
        repository.create( aProxy );
        System.out.println( "Created " + aProxy.getIdentity() );
    }

    public void read( PersistenceBinding aProxy ) throws ObjectNotFoundException
    {
        repository.read( aProxy );
        System.out.println( "Read " + aProxy.getIdentity() );
    }

    public void update( PersistenceBinding aProxy, Object aMixin )
    {
        repository.update( aProxy, aMixin );
        System.out.println( "Updated mixin " + aMixin.getClass().getSimpleName() + " for " + aProxy.getIdentity() );
    }

    public void delete( PersistenceBinding aProxy )
    {
        repository.delete( aProxy );
        System.out.println( "Deleted " + aProxy.getIdentity() );
    }
}
