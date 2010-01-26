/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.runtime.entity.EntityModel;

/**
 * JAVADOC
 */
class EntityFinder
    implements ModuleVisitor
{
    private final Class mixinType;
    private final List<ModuleInstance> modules;
    private final List<EntityModel> models;

    public EntityFinder( Class type )
    {
        mixinType = type;
        models = new ArrayList<EntityModel>();
        modules = new ArrayList<ModuleInstance>();
    }

    public boolean visitModule( final ModuleInstance moduleInstance,
                                ModuleModel moduleModel,
                                final Visibility visibility
    )
    {
        moduleModel.entities().visitModel( new ModelVisitor()
        {
            @Override
            public void visit( EntityModel entityModel )
            {
                if( EntityComposite.class.isAssignableFrom( mixinType ) )
                {
                    if( mixinType.equals( entityModel.type() ) && entityModel.visibility() == visibility )
                    {
                        modules.add( moduleInstance );
                        models.add( entityModel );
                    }
                }
                else
                {
                    if( mixinType.isAssignableFrom( entityModel.type() ) && entityModel.visibility() == visibility )
                    {
                        modules.add( moduleInstance );
                        models.add( entityModel );
                    }
                }
            }
        } );

        return true;
    }

    EntityModel getFoundModel()
    {
        return models.get( 0 );
    }

    boolean noModelExist()
    {
        return models.isEmpty();
    }

    boolean multipleModelsExists()
    {
        return models.size() > 1;
    }

    List<Class<?>> ambigousTypes()
    {
        List<Class<?>> ambiguousTypes = new ArrayList<Class<?>>();
        for( EntityModel model : models )
        {
            ambiguousTypes.add( model.type() );
        }
        return ambiguousTypes;
    }

    public ModuleInstance getFoundModule()
    {
        return modules.get( 0 );
    }

    public List<ModuleInstance> modules()
    {
        return modules;
    }

    public List<EntityModel> models()
    {
        return models;
    }
}
