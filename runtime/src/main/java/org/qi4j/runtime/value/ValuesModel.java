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

package org.qi4j.runtime.value;

import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;

/**
 * JAVADOC
 */
public final class ValuesModel
    implements Binder
{
    private final List<? extends ValueModel> valueModels;

    public ValuesModel( List<? extends ValueModel> valueModels )
    {
        this.valueModels = valueModels;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( ValueModel valueModel : valueModels )
        {
            valueModel.visitModel( modelVisitor );
        }
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        for( ValueModel valueModel : valueModels )
        {
            valueModel.bind( resolution );
        }
    }

    public ValueModel getValueModelFor( Class valueType, Visibility visibility )
    {
        ValueModel foundModel = null;
        if( ValueComposite.class.isAssignableFrom( valueType ) )
        {
            for( ValueModel valueModel : valueModels )
            {
                if( valueType.equals( valueModel.type() ) && valueModel.visibility() == visibility )
                {
                    if( foundModel != null )
                    {
                        throw new AmbiguousTypeException( valueType, foundModel.type(), valueModel.type() );
                    }
                    else
                    {
                        foundModel = valueModel;
                    }
                }
            }
        }
        else
        {
            for( ValueModel valueModel : valueModels )
            {
                if( valueType.isAssignableFrom( valueModel.type() ) && valueModel.visibility() == visibility )
                {
                    if( foundModel != null )
                    {
                        throw new AmbiguousTypeException( valueType, foundModel.type(), valueModel.type() );
                    }
                    else
                    {
                        foundModel = valueModel;
                    }
                }
            }
        }

        return foundModel;
    }

    public Class getClassForName( String type )
    {
        for( ValueModel valueModel : valueModels )
        {
            if( valueModel.type().getName().equals( type ) )
            {
                return valueModel.type();
            }
        }

        return null;
    }
}