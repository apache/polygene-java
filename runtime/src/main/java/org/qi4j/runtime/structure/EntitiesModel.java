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

package org.qi4j.runtime.structure;

import java.io.Serializable;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;

/**
 * JAVADOC
 */
public class EntitiesModel
    implements Binder, Serializable
{
    private final List<EntityModel> entityModels;

    public EntitiesModel( List<EntityModel> entityModels )
    {
        this.entityModels = entityModels;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( EntityModel entityModel : entityModels )
        {
            entityModel.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( EntityModel entityModel : entityModels )
        {
            entityModel.bind( resolution );
        }
    }

    public EntityModel getEntityModelFor( Class mixinType, Visibility visibility )
        throws AmbiguousTypeException
    {
        EntityModel foundModel = null;
        for( EntityModel entityModel : entityModels )
        {
            if( mixinType.isAssignableFrom( entityModel.type() ) && entityModel.visibility() == visibility )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousTypeException( mixinType, foundModel.type(), entityModel.type() );
                }
                else
                {
                    foundModel = entityModel;
                }
            }
        }

        return foundModel;
    }

    public Class getClassForName( String type )
    {
        for( EntityModel entityModel : entityModels )
        {
            if( entityModel.type().getName().equals( type ) )
            {
                return entityModel.type();
            }
        }

        return null;
    }
}
