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
package org.apache.polygene.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;
import org.apache.polygene.api.association.AbstractAssociation;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.GenericAssociationInfo;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.Aggregated;
import org.apache.polygene.api.entity.Queryable;
import org.apache.polygene.api.property.Immutable;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.util.Visitable;
import org.apache.polygene.api.util.Visitor;
import org.apache.polygene.bootstrap.BindingException;
import org.apache.polygene.runtime.composite.ValueConstraintsInstance;
import org.apache.polygene.runtime.model.Binder;
import org.apache.polygene.runtime.model.Resolution;

public class AbstractAssociationModel<AT>
    implements AbstractAssociation, AssociationDescriptor, AssociationInfo, Binder, Visitable<AT>
{
    private final MetaInfo metaInfo;
    private final AccessibleObject accessor;
    private final QualifiedName qualifiedName;
    private final ValueConstraintsInstance constraints;
    private final ValueConstraintsInstance associationConstraints;
    private final boolean queryable;
    private final boolean immutable;
    private final boolean aggregated;
    private Type type;
    private AssociationInfo builderInfo;

    public AbstractAssociationModel( AccessibleObject accessor,
                                     ValueConstraintsInstance valueConstraintsInstance,
                                     ValueConstraintsInstance associationConstraintsInstance,
                                     MetaInfo metaInfo )
    {
        Objects.requireNonNull( accessor );
        Objects.requireNonNull( metaInfo );
        this.metaInfo = metaInfo;
        this.constraints = valueConstraintsInstance;
        this.associationConstraints = associationConstraintsInstance;
        this.accessor = accessor;
        this.type = GenericAssociationInfo.associationTypeOf( accessor );
        this.qualifiedName = QualifiedName.fromAccessor( accessor );
        this.immutable = metaInfo.get( Immutable.class ) != null;
        this.aggregated = metaInfo.get( Aggregated.class ) != null;

        Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override
    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public Type type()
    {
        return type;
    }

    @Override
    public boolean isImmutable()
    {
        return immutable;
    }

    @Override
    public boolean isAggregated()
    {
        return aggregated;
    }

    @Override
    public AccessibleObject accessor()
    {
        return accessor;
    }

    @Override
    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        constraints.checkConstraints( value, accessor );
    }

    public void checkAssociationConstraints( AbstractAssociation association )
        throws ConstraintViolationException
    {
        associationConstraints.checkConstraints( association, accessor );
    }

    public AssociationInfo builderInfo()
    {
        return builderInfo;
    }

    @Override
    public void bind( Resolution resolution )
        throws BindingException
    {
        builderInfo = new AssociationInfo()
        {
            @Override
            public boolean isImmutable()
            {
                return false;
            }

            @Override
            public QualifiedName qualifiedName()
            {
                return qualifiedName;
            }

            @Override
            public Type type()
            {
                return type;
            }

            @Override
            public void checkConstraints( Object value )
                throws ConstraintViolationException
            {
                AbstractAssociationModel.this.checkConstraints( value );
            }
        };

        if( type instanceof TypeVariable )
        {
            Class mainType = resolution.model().types().findFirst().orElse( null );
            type = Classes.resolveTypeVariable( (TypeVariable) type, ( (Member) accessor ).getDeclaringClass(), mainType );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super AT, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( (AT) this );
    }

    @Override
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
        AbstractAssociationModel that = (AbstractAssociationModel) o;
        return accessor.equals( that.accessor );
    }

    @Override
    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        if( accessor instanceof Field )
        {
            return ( (Field) accessor ).toGenericString();
        }
        else
        {
            return ( (Method) accessor ).toGenericString();
        }
    }
}
