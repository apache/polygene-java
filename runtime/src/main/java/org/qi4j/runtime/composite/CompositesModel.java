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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.Composite;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * JAVADOC
 */
public class CompositesModel
    implements Binder, Serializable
{
    private final List<? extends TransientModel> compositeModels;

    public CompositesModel( List<? extends TransientModel> compositeModels )
    {
        this.compositeModels = compositeModels;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( TransientModel transientModel : compositeModels )
        {
            transientModel.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( TransientModel transientModel : compositeModels )
        {
            transientModel.bind( resolution );
        }
    }

    public TransientModel getCompositeModelFor( Class mixinType, Visibility visibility )
    {
        TransientModel foundModel = null;
        for( TransientModel aTransient : compositeModels )
        {
            if( Composite.class.isAssignableFrom( mixinType ) )
            {
                if( mixinType.equals( aTransient.type() ) && aTransient.visibility() == visibility )
                {
                    if( foundModel != null )
                    {
                        throw new AmbiguousTypeException( mixinType, foundModel.type(), aTransient.type() );
                    }
                    else
                    {
                        foundModel = aTransient;
                    }
                }
            }
            else
            {
                if( mixinType.isAssignableFrom( aTransient.type() ) && aTransient.visibility() == visibility )
                {
                    if( foundModel != null )
                    {
                        throw new AmbiguousTypeException( mixinType, foundModel.type(), aTransient.type() );
                    }
                    else
                    {
                        foundModel = aTransient;
                    }
                }
            }
        }

        return foundModel;
    }

    public Class getClassForName( String type )
    {
        for( TransientModel transientModel : compositeModels )
        {
            if( transientModel.type().getName().equals( type ) )
            {
                return transientModel.type();
            }
        }

        return null;
    }
}
