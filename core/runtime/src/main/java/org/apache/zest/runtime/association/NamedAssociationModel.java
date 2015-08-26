/*
 * Copyright (c) 2011-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.GenericAssociationInfo;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.constraint.ConstraintViolation;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.entity.Aggregated;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.util.Classes;
import org.apache.zest.bootstrap.BindingException;
import org.apache.zest.functional.Visitable;
import org.apache.zest.functional.Visitor;
import org.apache.zest.runtime.composite.ValueConstraintsInstance;
import org.apache.zest.runtime.model.Binder;
import org.apache.zest.runtime.model.Resolution;
import org.apache.zest.runtime.structure.ModuleUnitOfWork;
import org.apache.zest.runtime.unitofwork.BuilderEntityState;
import org.apache.zest.spi.entity.EntityState;

import static org.apache.zest.functional.Iterables.empty;
import static org.apache.zest.functional.Iterables.first;

/**
 * Model for a NamedAssociation.
 *
 * <p>Equality is based on the NamedAssociation accessor object (associated type and name), not on the QualifiedName.</p>
 */
public final class NamedAssociationModel
    implements AssociationDescriptor, AssociationInfo, Binder, Visitable<NamedAssociationModel>
{
    private final ValueConstraintsInstance associationConstraints;
    private final MetaInfo metaInfo;
    private Type type;
    private final AccessibleObject accessor;
    private QualifiedName qualifiedName;
    private final ValueConstraintsInstance constraints;
    private boolean queryable;
    private boolean immutable;
    private boolean aggregated;
    private AssociationInfo builderInfo;

    public NamedAssociationModel( AccessibleObject accessor,
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

    public <T> NamedAssociation<T> newInstance( final ModuleUnitOfWork uow, EntityState state )
    {
        return new NamedAssociationInstance<>( state instanceof BuilderEntityState ? builderInfo : this, new BiFunction<EntityReference, Type, Object>()
        {
            @Override
            public Object apply( EntityReference entityReference, Type type )
            {
                return uow.get( Classes.RAW_CLASS.apply( type ), entityReference.identity() );
            }
        }, state.namedAssociationValueOf( qualifiedName ) );
    }

    @Override
    public void checkConstraints( Object composite )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( composite );
            if( !violations.isEmpty() )
            {
                Stream<Class<?>> empty = Stream.empty();
                throw new ConstraintViolationException( "", empty, (Member) accessor, violations );
            }
        }
    }

    public void checkAssociationConstraints( NamedAssociation association )
        throws ConstraintViolationException
    {
        if( associationConstraints != null )
        {
            List<ConstraintViolation> violations = associationConstraints.checkConstraints( association );
            if( !violations.isEmpty() )
            {
                Stream<Class<?>> empty = Stream.empty();
                throw new ConstraintViolationException( "", empty, (Member) accessor, violations );
            }
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super NamedAssociationModel, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
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
                NamedAssociationModel.this.checkConstraints( value );
            }
        };

        if( type instanceof TypeVariable )
        {
            Class mainType = resolution.model().types().findFirst().orElse( null );
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

        NamedAssociationModel that = (NamedAssociationModel) o;

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
