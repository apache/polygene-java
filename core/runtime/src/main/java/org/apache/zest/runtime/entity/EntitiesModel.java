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

package org.apache.zest.runtime.entity;

import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;

/**
 * Model of entities in a particular Module.
 */
public class EntitiesModel
    implements VisitableHierarchy<Object, Object>
{
    private final List<EntityModel> entityModels;

    public EntitiesModel( List<EntityModel> entityModels )
    {
        this.entityModels = entityModels;
    }

    public Stream<EntityModel> models()
    {
        return entityModels.stream();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            for( EntityModel entityModel : entityModels )
            {
                if( !entityModel.accept( modelVisitor ) )
                {
                    break;
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    public Stream<? extends EntityDescriptor> stream()
    {
        return entityModels.stream();
    }
}
