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

package org.qi4j.runtime.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.EntityAssembly;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.ConcernDeclaration;
import org.qi4j.runtime.composite.ConcernsDeclaration;
import org.qi4j.runtime.entity.EntityModel;

/**
 * Declaration of a EntityComposite.
 */
public final class EntityAssemblyImpl
    implements EntityAssembly
{
    private Class<? extends EntityComposite> compositeType;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> roles = new ArrayList<Class<?>>();

    public EntityAssemblyImpl( Class<? extends EntityComposite> compositeType )
    {
        this.compositeType = compositeType;
    }

    @Override
    public Class<? extends EntityComposite> type()
    {
        return compositeType;
    }

    void addEntityModel( List<EntityModel> entities,
                         PropertyDeclarations propertyDecs,
                         AssociationDeclarations associationDecs,
                         ManyAssociationDeclarations manyAssociationDecs,
                         AssemblyHelper helper
    )
    {
        try
        {
            List<ConcernDeclaration> concernDeclarations = new ArrayList<ConcernDeclaration>();
            ConcernsDeclaration.concernDeclarations( concerns, concernDeclarations );
            ConcernsDeclaration.concernDeclarations( compositeType, concernDeclarations );
            ConcernsDeclaration concernsDeclaration = new ConcernsDeclaration( concernDeclarations );

            EntityModel compositeModel = EntityModel.newModel( compositeType,
                                                               visibility,
                                                               new MetaInfo( metaInfo ).withAnnotations( compositeType ),
                                                               propertyDecs,
                                                               associationDecs,
                                                               manyAssociationDecs,
                                                               concernsDeclaration,
                                                               sideEffects,
                                                               mixins,
                                                               roles,
                                                               helper );
            entities.add( compositeModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }
}