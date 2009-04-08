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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.entity.RDF;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.property.Immutable;
import static org.qi4j.api.util.Classes.getRawClass;
import org.qi4j.api.util.SerializationUtil;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * JAVADOC
 */
public final class AssociationModel
    implements AssociationDescriptor, Serializable
{
    private MetaInfo metaInfo;
    private Type type;
    private Method accessor;
    private QualifiedName qualifiedName;
    private String rdf;
    private ValueConstraintsInstance constraints;
    private boolean queryable;
    private boolean immutable;
    private boolean aggregated;

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

    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        metaInfo = (MetaInfo) in.readObject();
        accessor = SerializationUtil.readMethod( in );
        constraints = (ValueConstraintsInstance) in.readObject();
        initialize();
    }


    public AssociationModel( Method accessor, ValueConstraintsInstance valueConstraintsInstance, MetaInfo metaInfo )
    {
        this.metaInfo = metaInfo;
        this.constraints = valueConstraintsInstance;
        this.accessor = accessor;
        initialize();
    }

    private void initialize()
    {
        this.type = GenericAssociationInfo.getAssociationType( accessor );
        this.qualifiedName = QualifiedName.fromMethod( accessor );
        this.immutable = metaInfo.get( Immutable.class ) != null;
        this.aggregated = metaInfo.get( Aggregated.class ) != null;
        RDF uriAnnotation = accessor().getAnnotation( RDF.class );
        this.rdf = uriAnnotation == null ? null : uriAnnotation.value();

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

    public boolean isManyAssociation()
    {
        return ManyAssociation.class.isAssignableFrom( accessor.getReturnType() );
    }

    public boolean isListAssociation()
    {
        return ListAssociation.class.isAssignableFrom( accessor.getReturnType() );
    }

    public boolean isSetAssociation()
    {
        return SetAssociation.class.isAssignableFrom( accessor.getReturnType() );
    }

    public boolean isAssociation()
    {
        return Association.class.isAssignableFrom( accessor.getReturnType() );
    }

    public AbstractAssociation newDefaultInstance()
    {
        AbstractAssociation instance;
        if( isListAssociation() )
        {
            instance = new EntityBuilderListAssociation<Object>();
        }
        else if( isSetAssociation() )
        {
            instance = new EntityBuilderSetAssociation<Object>();
        }
        else if( isManyAssociation() )
        {
            instance = new EntityBuilderListAssociation<Object>();
        }
        else
        {
            instance = new EntityBuilderAssociation<Object>( this );
        }
        return instance;
    }

    public AbstractAssociation newInstance( ModuleUnitOfWork uow, EntityState state )
    {
        AbstractAssociation associationInstance;
        if( !isManyAssociation() )
        {
            associationInstance = new AssociationInstance<Object>( this, uow, state );
        }
        else
        {
            if( isListAssociation() )
            {
                associationInstance = new ListAssociationInstance<Object>( this, uow, state );
            }
            else if( isSetAssociation() )
            {
                associationInstance = new SetAssociationInstance<Object>( this, uow, state );
            }
            else
            {
                associationInstance = new ManyAssociationInstance<Object>( this, uow, state );
            }
        }

        if( Composite.class.isAssignableFrom( accessor.getReturnType() ) )
        {
            associationInstance = (AbstractAssociation) uow.module().compositeBuilderFactory().newCompositeBuilder( accessor.getReturnType() ).use( associationInstance ).newInstance();
        }

        return associationInstance;
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value, false );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( accessor, violations );
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

    @Override public String toString()
    {
        return accessor.toGenericString();
    }

    public void setState( AbstractAssociation association, EntityState entityState )
    {
        if( association != null )
        {
            if( isManyAssociation() )
            {
                ManyAssociation<?> manyAssociation = (ManyAssociation<?>) association;
                Collection<QualifiedIdentity> stateCollection = entityState.getManyAssociation( qualifiedName );
                for( Object associated : manyAssociation )
                {
                    stateCollection.add( QualifiedIdentity.getQualifiedIdentity( associated ) );
                }
            }
            else
            {
                Association<?> assoc = (Association<?>) association;
                Object associated = assoc.get();

                checkConstraints( associated );

                if( associated != null )
                {
                    entityState.setAssociation( qualifiedName, QualifiedIdentity.getQualifiedIdentity( associated ) );
                }
            }
        }
    }

    public AssociationType associationType()
    {
        return new AssociationType( qualifiedName, getRawClass( type ).getName(), rdf, queryable );
    }

    public ManyAssociationType manyAssociationType()
    {
        ManyAssociationType.ManyAssociationTypeEnum manyAssocType;
        if( isListAssociation() )
        {
            manyAssocType = ManyAssociationType.ManyAssociationTypeEnum.LIST;
        }
        else if( isSetAssociation() )
        {
            manyAssocType = ManyAssociationType.ManyAssociationTypeEnum.SET;
        }
        else
        {
            manyAssocType = ManyAssociationType.ManyAssociationTypeEnum.MANY;
        }
        return new ManyAssociationType( qualifiedName, manyAssocType, getRawClass( type ).getName(), rdf, queryable );
    }
}
