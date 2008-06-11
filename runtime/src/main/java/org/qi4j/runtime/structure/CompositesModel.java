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
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class CompositesModel
    implements Binder
{
    private List<CompositeModel> compositeModels;

    public CompositesModel( List<CompositeModel> compositeModels )
    {
        this.compositeModels = compositeModels;
    }


    public void visitModel( ModelVisitor modelVisitor )
    {
        for( CompositeModel compositeModel : compositeModels )
        {
            compositeModel.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution resolution ) throws BindingException
    {
        for( CompositeModel compositeModel : compositeModels )
        {
            compositeModel.bind( resolution );
        }
    }

    public CompositeModel getCompositeModelFor( Class mixinType, Visibility visibility )
    {
        CompositeModel foundModel = null;
        for( CompositeModel composite : compositeModels )
        {
            if( mixinType.isAssignableFrom( composite.type() ) && composite.visibility() == visibility )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                else
                {
                    foundModel = composite;
                }
            }
        }

        return foundModel;
    }

    public CompositeModel getCompositeModelFor( Class mixinType )
    {
        CompositeModel compositeModel = getCompositeModelFor( mixinType, Visibility.module );
        if( compositeModel == null )
        {
            compositeModel = getCompositeModelFor( mixinType, Visibility.layer );
        }
        if( compositeModel == null )
        {
            compositeModel = getCompositeModelFor( mixinType, Visibility.application );
        }

        return compositeModel;
    }

    public Class getClassForName( String type )
    {
        for( CompositeModel compositeModel : compositeModels )
        {
            if( compositeModel.type().getName().equals( type ) )
            {
                return compositeModel.type();
            }
        }

        return null;
    }
}
