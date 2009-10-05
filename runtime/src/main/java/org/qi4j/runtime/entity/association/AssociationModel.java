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

package org.qi4j.runtime.entity.association;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public final class AssociationModel
    implements AssociationDescriptor, Serializable, ConstraintsCheck
{
    private MetaInfo metaInfo;
    private Type type;
    private Method accessor;
    private QualifiedName qualifiedName;
    private ValueConstraintsInstance constraints;
    private ValueConstraintsInstance associationConstraints;
    private boolean queryable;
    private boolean immutable;
    private boolean aggregated;
    private AssociationType associationType;
    private AssociationInfo builderInfo;

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        try
        {
            out.writeObject( metaInfo );
            SerializationUtil.writeMethod( out, accessor );
            out.writeObject( constraints );
        }
        catch( NotSerializableException e )
        {
            System.err.println( "NotSerializable in " + getClass() );
            throw e;
        }
    }

    private void readObject( ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        metaInfo = (MetaInfo) in.readObject();
        accessor = SerializationUtil.readMethod( in );
        constraints = (ValueConstraintsInstance) in.readObject();
        initialize();
    }

    public AssociationModel( Method accessor,
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
        this.associationType = new AssociationType( qualifiedName, TypeName.nameOf( type ), queryable );
        builderInfo = new GenericAssociationInfo( accessor, metaInfo, false );
    }

    private void initialize()
    {
        this.type = GenericAssociationInfo.getAssociationType( accessor );
        this.qualifiedName = QualifiedName.fromMethod( accessor );
        this.immutable = metaInfo.get( Immutable.class ) != null;
        this.aggregated = metaInfo.get( Aggregated.class ) != null;

        final Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isAggregated()
    {
        return aggregated;
    }

    public Method accessor()
    {
        return accessor;
    }

    public boolean isAssociation()
    {
        return Association.class.isAssignableFrom( accessor.getReturnType() );
    }

    public <T> Association<T> newInstance( ModuleUnitOfWork uow, EntityState state )
    {
        Association<T> associationInstance = new AssociationInstance<T>( state instanceof BuilderEntityState ? builderInfo : this, this, uow, state );

        if( Composite.class.isAssignableFrom( accessor.getReturnType() ) )
        {
            associationInstance = (Association<T>) uow.module()
                .transientBuilderFactory()
                .newTransientBuilder( accessor.getReturnType() )
                .use( associationInstance )
                .newInstance();
        }

        return associationInstance;
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( "", "<unknown>", accessor, violations );
            }
        }
    }

    public void checkConstraints( EntityAssociationsInstance associations )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            Object value = associations.associationFor( accessor ).get();
            checkConstraints( value );
        }
    }

    public void checkAssociationConstraints( EntityAssociationsInstance associationsInstance )
        throws ConstraintViolationException
    {
        if( associationConstraints != null )
        {
            Association association = associationsInstance.associationFor( accessor );

            List<ConstraintViolation> violations = associationConstraints.checkConstraints( association );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( (Composite) association.get(), accessor, violations );
            }
        }
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

        AssociationModel that = (AssociationModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        return accessor.toGenericString();
    }

    public AssociationType associationType()
    {
        return associationType;
    }
}
