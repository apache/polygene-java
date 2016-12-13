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
package org.apache.zest.runtime.composite;

import java.lang.reflect.AccessibleObject;
import java.util.stream.Stream;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.composite.StateDescriptor;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;
import org.apache.zest.runtime.property.PropertiesModel;
import org.apache.zest.runtime.property.PropertyModel;

/**
 * Base model for Composite state
 */
public class StateModel
    implements StateDescriptor, VisitableHierarchy<Object, Object>
{
    protected final PropertiesModel propertiesModel;

    public StateModel( PropertiesModel propertiesModel )
    {
        this.propertiesModel = propertiesModel;
    }

    public PropertyModel propertyModelFor( AccessibleObject accessor )
    {
        return propertiesModel.getProperty( accessor );
    }

    @Override
    public PropertyModel findPropertyModelByName( String name )
        throws IllegalArgumentException
    {
        return propertiesModel.getPropertyByName( name );
    }

    @Override
    public PropertyModel findPropertyModelByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        return propertiesModel.getPropertyByQualifiedName( name );
    }

    @Override
    public Stream<PropertyModel> properties()
    {
        return propertiesModel.properties();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            ( (VisitableHierarchy<Object, Object>) propertiesModel ).accept( visitor );
        }
        return visitor.visitLeave( this );
    }
}
