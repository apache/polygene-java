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

package org.qi4j.spi.composite;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;

/**
 * TODO
 */
public class AbstractMethodModel
    implements Serializable
{
    private Method method;
    private Iterable<ParameterModel> parameterModels;
    private PropertyModel propertyModel;
    private AssociationModel associationModel;

    public AbstractMethodModel( Method method, Iterable<ParameterModel> parameters, PropertyModel propertyModel, AssociationModel associationModel )
    {
        this.method = method;
        this.parameterModels = parameters;
        this.propertyModel = propertyModel;
        this.associationModel = associationModel;
    }

    public Method getMethod()
    {
        return method;
    }

    public Iterable<ParameterModel> getParameterModels()
    {
        return parameterModels;
    }

    public PropertyModel getPropertyModel()
    {
        return propertyModel;
    }

    public AssociationModel getAssociationModel()
    {
        return associationModel;
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AbstractMethodModel that = (AbstractMethodModel) o;

        return method.equals( that.method );
    }

    public int hashCode()
    {
        return method.hashCode();
    }


    @Override public String toString()
    {
        return method.toGenericString();
    }
}
