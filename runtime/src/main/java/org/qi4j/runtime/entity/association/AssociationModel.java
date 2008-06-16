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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConstraintViolation;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public final class AssociationModel
    implements AssociationDescriptor
{
    private final MetaInfo metaInfo;
    private final String name;
    private final Type type;
    private final Method accessor;
    private final String qualifiedName;
    private final ValueConstraintsInstance constraints;

    public AssociationModel( Method accessor, ValueConstraintsInstance valueConstraintsInstance, MetaInfo metaInfo )
    {
        this.metaInfo = metaInfo;
        name = accessor.getName();
        type = AbstractAssociationInstance.getAssociationType( accessor );
        this.accessor = accessor;
        qualifiedName = AbstractAssociationInstance.getQualifiedName( accessor );

        this.constraints = valueConstraintsInstance;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public Method accessor()
    {
        return accessor;
    }

    public String toURI()
    {
        return AbstractAssociationInstance.toURI( accessor );
    }

    public String toNameSpace()
    {
        return "urn:qi4j:association:" + ComputedPropertyInstance.getDeclaringClassName( accessor ) + ":";
    }

    public AbstractAssociation newDefaultInstance()
    {
        AbstractAssociation instance;
        if( isListAssociation( accessor.getReturnType() ) )
        {
            instance = new EntityBuilderListAssociation<Object>();
        }
        else if( isSetAssociation( accessor.getReturnType() ) )
        {
            instance = new EntityBuilderSetAssociation<Object>();
        }
        else if( isManyAssociation( accessor.getReturnType() ) )
        {
            instance = new EntityBuilderListAssociation<Object>();
        }
        else
        {
            instance = new EntityBuilderAssociation<Object>();
        }
        return instance;
    }

    public AbstractAssociation newInstance( UnitOfWorkInstance uow, EntityState state )
    {
        final Class<?> associationType = accessor.getReturnType();
        if( !isManyAssociation( associationType ) )
        {
            return new AssociationInstance<Object>( this, uow, state );
        }

        Collection<QualifiedIdentity> manyAssociation = getManyAssociation( state, associationType );

        if( isListAssociation( associationType ) )
        {
            return new ListAssociationInstance<Object>( this, uow, (List<QualifiedIdentity>) manyAssociation );
        }
        if( isSetAssociation( associationType ) )
        {
            return new SetAssociationInstance<Object>( this, uow, (Set<QualifiedIdentity>) manyAssociation );
        }
        return new ManyAssociationInstance<Object>( this, uow, manyAssociation );
    }

    private boolean isManyAssociation( Class<?> associationType )
    {
        return ManyAssociation.class.isAssignableFrom( associationType );
    }

    private boolean isListAssociation( Class<?> associationType )
    {
        return ListAssociation.class.isAssignableFrom( associationType );
    }

    private Collection<QualifiedIdentity> getManyAssociation( EntityState state, Class<?> associationType )
    {
        Collection<QualifiedIdentity> manyAssociation = state.getManyAssociation( qualifiedName );
        if( manyAssociation != null )
        {
            return manyAssociation;
        }

        if ( isSetAssociation( associationType ) ) {
            return state.setManyAssociation( qualifiedName, new HashSet<QualifiedIdentity>() );
        } else {
            return state.setManyAssociation( qualifiedName, new ArrayList<QualifiedIdentity>() );
        }
    }

    private boolean isSetAssociation( Class<?> associationType )
    {
        return SetAssociation.class.isAssignableFrom( associationType );
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
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
            if( isManyAssociation( accessor.getReturnType() ) )
            {
                ManyAssociation<Composite> manyAssociation = (ManyAssociation<Composite>) association;
                Collection<QualifiedIdentity> stateCollection = entityState.getManyAssociation( qualifiedName );
                for( Composite entity : manyAssociation )
                {
                    EntityInstance instance = EntityInstance.getEntityInstance( entity );
                    stateCollection.add( instance.identity() );
                }
            }
            else
            {
                Association<Composite> assoc = (Association<Composite>) association;
                Composite composite = assoc.get();
                if( composite != null )
                {
                    EntityInstance instance = EntityInstance.getEntityInstance( composite );
                    entityState.setAssociation( qualifiedName, instance.identity() );
                }
            }
        }
    }
}
