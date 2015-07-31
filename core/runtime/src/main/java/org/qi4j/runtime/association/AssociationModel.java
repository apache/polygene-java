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

package org.qi4j.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.functional.Visitable;
import org.qi4j.functional.Visitor;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;

import static org.qi4j.functional.Iterables.empty;
import static org.qi4j.functional.Iterables.first;

/**
 * Model for an Association.
 *
 * <p>Equality is based on the Association accessor object (associated type and name), not on the QualifiedName.</p>
 */
public final class AssociationModel
    implements AssociationDescriptor, AssociationInfo, Binder, Visitable<AssociationModel>
{
    private MetaInfo metaInfo;
    private Type type;
    private AccessibleObject accessor;
    private QualifiedName qualifiedName;
    private ValueConstraintsInstance constraints;
    private ValueConstraintsInstance associationConstraints;
    private boolean queryable;
    private boolean immutable;
    private boolean aggregated;
    private AssociationInfo builderInfo;

    public AssociationModel( AccessibleObject accessor,
                             ValueConstraintsInstance valueConstraintsInstance,
                             ValueConstraintsInstance associationConstraintsInstance,
                             MetaInfo metaInfo
    )
    {
        this.metaInfo = metaInfo;
        this.constraints = valueConstraintsInstance;
        this.associationConstraints = associationConstraintsInstance;
        this.accessor = accessor;
        initialize();
    }

    private void initialize()
    {
        this.type = GenericAssociationInfo.associationTypeOf( accessor );
        this.qualifiedName = QualifiedName.fromAccessor( accessor );
        this.immutable = metaInfo.get( Immutable.class ) != null;
        this.aggregated = metaInfo.get( Aggregated.class ) != null;

        final Queryable queryable = accessor.getAnnotation( Queryable.class );
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

    public AssociationInfo getBuilderInfo()
    {
        return builderInfo;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super AssociationModel, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }

    @Override
    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                Iterable<Class<?>> empty = empty();
                throw new ConstraintViolationException( "", empty, (Member) accessor, violations );
            }
        }
    }

    public void checkAssociationConstraints( Association<?> association )
        throws ConstraintViolationException
    {
        if( associationConstraints != null )
        {
            List<ConstraintViolation> violations = associationConstraints.checkConstraints( association );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( (Composite) association.get(), (Member) accessor, violations );
            }
        }
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
                AssociationModel.this.checkConstraints( value );
            }
        };

        if( type instanceof TypeVariable )
        {

            Class mainType = first( resolution.model().types() );
            type = Classes.resolveTypeVariable( (TypeVariable) type, ( (Member) accessor ).getDeclaringClass(), mainType );
        }
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

        AssociationModel that = (AssociationModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
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
