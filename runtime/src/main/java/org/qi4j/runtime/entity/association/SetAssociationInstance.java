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

package org.qi4j.runtime.entity.association;

import java.util.Set;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.SetAssociation;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Implementation of SetAssociation, which delegates to a
 * Set provided by the EntityStore.
 */
public final class SetAssociationInstance<T>
    extends ManyAssociationInstance<T>
    implements SetAssociation<T>
{
    public SetAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork, Set<QualifiedIdentity> associated )
    {
        super( associationInfo, unitOfWork, associated );
    }

    public void refresh( Set<QualifiedIdentity> newSet )
    {
        super.refresh( newSet );
    }
}