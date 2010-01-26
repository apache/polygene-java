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
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Immutable;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.util.SerializationUtil;

import static org.qi4j.api.util.Classes.*;

/**
 * JAVADOC
 */
public final class ManyAssociationModel
    implements ManyAssociationDescriptor, ConstraintsCheck, Serializable
{
    private ValueConstraintsInstance associationConstraints;
    private MetaInfo metaInfo;
    private Type type;
    private Method accessor;
    private QualifiedName qualifiedName;
    private ValueConstraintsInstance constraints;
    private boolean queryable;
    private boolean immutable;
    private boolean aggregated;
    private ManyAssociationType manyAssociationType;
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

    public ManyAssociationModel( Method accessor,
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
        this.manyAssociationType = new ManyAssociationType( qualifiedName, getRawClass( type ).getName(), queryable );
        this.builderInfo = new GenericAssociationInfo( accessor, metaInfo, false );
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

    public ManyAssociationType manyAssociationType()
    {
        return manyAssociationType;
    }

    public <T> ManyAssociation<T> newInstance( ModuleUnitOfWork uow, EntityState state )
    {
        ManyAssociation<T> associationInstance = new ManyAssociationInstance<T>( state instanceof BuilderEntityState ? builderInfo : this, this, uow, state );

        if( TransientComposite.class.isAssignableFrom( accessor.getReturnType() ) )
        {
            associationInstance = (ManyAssociation<T>) uow.module()
                .transientBuilderFactory()
                .newTransientBuilder( accessor.getReturnType() )
                .use( associationInstance )
                .newInstance();
        }

        return associationInstance;
    }

    public void checkConstraints( Object composite )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( composite );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( "", "<unknown>", accessor, violations );
            }
        }
    }

    public void checkAssociationConstraints( ManyAssociation manyAssociation )
        throws ConstraintViolationException
    {
        if( associationConstraints != null )
        {
            List<ConstraintViolation> violations = associationConstraints.checkConstraints( manyAssociation );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( "", "<unknown>", accessor, violations );
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

        ManyAssociationModel that = (ManyAssociationModel) o;

        return accessor.equals( that.accessor );
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
}