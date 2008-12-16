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

import java.util.List;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.common.MetaInfo;

/**
 * Declaration of a Composite. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addComposites(Class[])}.
 */
public final class EntityDeclarationImpl implements EntityDeclaration
{
    private Class<? extends EntityComposite>[] compositeTypes;
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public EntityDeclarationImpl( Class<? extends EntityComposite>... compositeTypes )
    {
        this.compositeTypes = compositeTypes;
    }

    public EntityDeclaration setMetaInfo( Object info )
    {
        metaInfo.set( info );
        return this;
    }

    public EntityDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    void addEntities( List<EntityModel> entities, PropertyDeclarations propertyDecs, AssociationDeclarations associationDecs )
    {
        for( Class<? extends EntityComposite> compositeType : compositeTypes )
        {
            EntityModel compositeModel = EntityModel.newModel( compositeType,
                                                               visibility,
                                                               new MetaInfo( metaInfo ).withAnnotations( compositeType ),
                                                               propertyDecs,
                                                               associationDecs );
            entities.add( compositeModel );
        }
    }
}