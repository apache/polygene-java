/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.util.MethodKeyMap;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;

/**
 * TODO
 */
public final class AssociationsInstance
{
    private Map<Method, AbstractAssociation> associations;
    private final AssociationsModel associationsModel;
    private final UnitOfWorkInstance uow;
    private final EntityState state;

    public AssociationsInstance( AssociationsModel associationsModel, UnitOfWorkInstance uow, EntityState state )
    {
        this.associationsModel = associationsModel;
        this.uow = uow;
        this.state = state;
    }

    public AbstractAssociation associationFor( Method accessor )
    {
        if( associations == null )
        {
            associations = new MethodKeyMap<AbstractAssociation>();
        }

        AbstractAssociation association = associations.get( accessor );

        if( association == null )
        {
            if( state == null )
            {
                association = associationsModel.newDefaultInstance( accessor );
            }
            else
            {
                association = associationsModel.newInstance( accessor, state, uow );
            }
            associations.put( accessor, association );
        }

        return association;
    }
}
