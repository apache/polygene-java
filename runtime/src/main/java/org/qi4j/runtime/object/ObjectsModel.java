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

package org.qi4j.runtime.object;

import java.io.Serializable;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * JAVADOC
 */
public class ObjectsModel
    implements Binder, Serializable
{
    private final List<ObjectModel> objectModels;

    public ObjectsModel( List<ObjectModel> objectModels )
    {
        this.objectModels = objectModels;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( ObjectModel objectModel : objectModels )
        {
            objectModel.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( ObjectModel objectModel : objectModels )
        {
            objectModel.bind( resolution );
        }
    }

    public ObjectModel getObjectModelFor( Class type, Visibility visibility )
    {
        ObjectModel foundModel = null;
        for( ObjectModel objectModel : objectModels )
        {
            if( type.isAssignableFrom( objectModel.type() ) && objectModel.visibility() == visibility )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousTypeException( type, foundModel.type(), objectModel.type() );
                }
                else
                {
                    foundModel = objectModel;
                }
            }
        }

        return foundModel;
    }

    public Class getClassForName( String type )
    {
        for( ObjectModel objectModel : objectModels )
        {
            if( objectModel.type().getName().equals( type ) )
            {
                return objectModel.type();
            }
        }
        return null;
    }
}
