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

import java.util.List;
import org.qi4j.composite.AmbiguousTypeException;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class EntitiesModel
    implements Binder
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

    public void bind( Resolution resolution ) throws BindingException
    {
        for( EntityModel entityModel : entityModels )
        {
            entityModel.bind( resolution );
        }
    }

    public EntityModel getEntityModelFor( Class mixinType, Visibility visibility )
    {
        EntityModel foundModel = null;
        for( EntityModel entityModel : entityModels )
        {
            if( mixinType.isAssignableFrom( entityModel.type() ) && entityModel.visibility() == visibility )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                else
                {
                    foundModel = entityModel;
                }
            }
        }

        return foundModel;
    }

    public EntityModel getEntityModelFor( Class mixinType )
    {
        EntityModel entityModel = getEntityModelFor( mixinType, Visibility.module );
        if( entityModel == null )
        {
            entityModel = getEntityModelFor( mixinType, Visibility.layer );
        }
        if( entityModel == null )
        {
            entityModel = getEntityModelFor( mixinType, Visibility.application );
        }

        return entityModel;
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
