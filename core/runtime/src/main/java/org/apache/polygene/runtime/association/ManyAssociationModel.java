/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.association;

import java.lang.reflect.AccessibleObject;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.runtime.composite.ValueConstraintsInstance;
import org.apache.polygene.runtime.unitofwork.BuilderEntityState;
import org.apache.polygene.runtime.unitofwork.ModuleUnitOfWork;
import org.apache.polygene.spi.entity.EntityState;

/**
 * Model for a ManyAssociation.
 *
 * <p>Equality is based on the ManyAssociation accessor object (associated type and name), not on the QualifiedName.</p>
 */
public final class ManyAssociationModel extends AbstractAssociationModel<ManyAssociationModel>
{
    public ManyAssociationModel( AccessibleObject accessor,
                                 ValueConstraintsInstance valueConstraintsInstance,
                                 ValueConstraintsInstance associationConstraintsInstance,
                                 MetaInfo metaInfo
    )
    {
        super( accessor, valueConstraintsInstance, associationConstraintsInstance, metaInfo );
    }

    public <T> ManyAssociation<T> newInstance( final ModuleUnitOfWork uow, EntityState state )
    {
        return new ManyAssociationInstance<>(
            state instanceof BuilderEntityState ? builderInfo() : this,
            ( entityReference, type ) -> uow.get( Classes.RAW_CLASS.apply( type ), entityReference.identity() ),
            state.manyAssociationValueOf( qualifiedName() )
        );
    }
}
