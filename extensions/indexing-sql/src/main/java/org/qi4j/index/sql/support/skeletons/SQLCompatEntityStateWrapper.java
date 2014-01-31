/*
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
package org.qi4j.index.sql.support.skeletons;

import java.util.Date;
import org.joda.money.BigMoney;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.NamedAssociationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EntityState wrapper used by AbstractSQLIndexing to filter out unsupported properties.
 *
 * <p>This allows to disable unsupported properties indexing to prevent failures in the SQL Index/Query engine.</p>
 * <p>When an unsupported Property is found it is logged at WARN level.</p>
 */
/* package */ class SQLCompatEntityStateWrapper
    implements EntityState
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SQLCompatEntityStateWrapper.class.getName() );

    /* package */ static final Function<EntityState, EntityState> WRAP = new Function<EntityState, EntityState>()
    {

        @Override
        public EntityState map( EntityState from )
        {
            return new SQLCompatEntityStateWrapper( from );
        }
    };
    private static final Specification<PropertyDescriptor> PROPERTY_SPEC = new Specification<PropertyDescriptor>()
    {

        @Override
        public boolean satisfiedBy( PropertyDescriptor propertyDescriptor )
        {
            boolean supported = isSupported( propertyDescriptor.valueType() );
            if( !supported )
            {
                LOGGER.warn( "Unsupported Property type: " + propertyDescriptor );
            }
            return supported;
        }

        private boolean isSupported( ValueType valueType )
        {
            if( valueType instanceof CollectionType )
            {
                CollectionType collectionType = (CollectionType) valueType;
                return isSupported( collectionType.collectedType() );
            }
            Class<?> mainType = valueType.mainType();
            return Number.class.isAssignableFrom( mainType )
                   || Boolean.class.isAssignableFrom( mainType )
                   || Character.class.isAssignableFrom( mainType )
                   || Enum.class.isAssignableFrom( mainType )
                   || String.class.isAssignableFrom( mainType )
                   // || Date.class.isAssignableFrom( mainType )
                   // || DateTime.class.isAssignableFrom( mainType )
                   // || LocalDateTime.class.isAssignableFrom( mainType )
                   // || LocalDate.class.isAssignableFrom( mainType )
                   // || Money.class.isAssignableFrom( mainType )
                   // || BigMoney.class.isAssignableFrom( mainType )
                   || valueType instanceof ValueCompositeType;
        }
    };

    private final EntityState wrappedEntityState;

    /* package */ SQLCompatEntityStateWrapper( EntityState wrapped )
    {
        this.wrappedEntityState = wrapped;
    }

    @Override
    public EntityReference identity()
    {
        return wrappedEntityState.identity();
    }

    @Override
    public String version()
    {
        return wrappedEntityState.version();
    }

    @Override
    public long lastModified()
    {
        return wrappedEntityState.lastModified();
    }

    @Override
    public void remove()
    {
        wrappedEntityState.remove();
    }

    @Override
    public EntityStatus status()
    {
        return wrappedEntityState.status();
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return wrappedEntityState.isAssignableTo( type );
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        return new CompatEntityDescriptorWrapper( wrappedEntityState.entityDescriptor() );
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        return wrappedEntityState.propertyValueOf( stateName );
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object json )
    {
        wrappedEntityState.setPropertyValue( stateName, json );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        return wrappedEntityState.associationValueOf( stateName );
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        wrappedEntityState.setAssociationValue( stateName, newEntity );
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        return wrappedEntityState.manyAssociationValueOf( stateName );
    }

    @Override
    public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
    {
        return wrappedEntityState.namedAssociationValueOf( stateName );
    }

    @Override
    @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
    public boolean equals( Object obj )
    {
        return wrappedEntityState.equals( obj );
    }

    @Override
    public int hashCode()
    {
        return wrappedEntityState.hashCode();
    }

    private static class CompatEntityDescriptorWrapper
        implements EntityDescriptor
    {
        private final EntityDescriptor wrappedEntityDescriptor;

        private CompatEntityDescriptorWrapper( EntityDescriptor wrappedEntityDescriptor )
        {
            this.wrappedEntityDescriptor = wrappedEntityDescriptor;
        }

        @Override
        public AssociationStateDescriptor state()
        {
            return new CompatAssociationStateDescriptorWrapper( wrappedEntityDescriptor.state() );
        }

        @Override
        public boolean queryable()
        {
            return wrappedEntityDescriptor.queryable();
        }

        @Override
        public Class<?> primaryType()
        {
            return wrappedEntityDescriptor.primaryType();
        }

        @Override
        public Iterable<Class<?>> mixinTypes()
        {
            return wrappedEntityDescriptor.mixinTypes();
        }

        @Override
        public Visibility visibility()
        {
            return wrappedEntityDescriptor.visibility();
        }

        @Override
        public boolean isAssignableTo( Class<?> type )
        {
            return wrappedEntityDescriptor.isAssignableTo( type );
        }

        @Override
        public Iterable<Class<?>> types()
        {
            return wrappedEntityDescriptor.types();
        }

        @Override
        public <T> T metaInfo( Class<T> infoType )
        {
            return wrappedEntityDescriptor.metaInfo( infoType );
        }

        @Override
        @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
        public boolean equals( Object obj )
        {
            return wrappedEntityDescriptor.equals( obj );
        }

        @Override
        public int hashCode()
        {
            return wrappedEntityDescriptor.hashCode();
        }
    }

    private static class CompatAssociationStateDescriptorWrapper
        implements AssociationStateDescriptor
    {
        private final AssociationStateDescriptor wrappedAssociationStateDescriptor;

        private CompatAssociationStateDescriptorWrapper( AssociationStateDescriptor wrappedAssociationStateDescriptor )
        {
            this.wrappedAssociationStateDescriptor = wrappedAssociationStateDescriptor;
        }

        @Override
        public AssociationDescriptor getAssociationByName( String name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getAssociationByName( name );
        }

        @Override
        public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getAssociationByQualifiedName( name );
        }

        @Override
        public AssociationDescriptor getManyAssociationByName( String name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getManyAssociationByName( name );
        }

        @Override
        public AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getManyAssociationByQualifiedName( name );
        }

        @Override
        public AssociationDescriptor getNamedAssociationByName( String name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getNamedAssociationByName( name );
        }

        @Override
        public AssociationDescriptor getNamedAssociationByQualifiedName( QualifiedName name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.getNamedAssociationByQualifiedName( name );
        }

        @Override
        public Iterable<? extends AssociationDescriptor> associations()
        {
            return wrappedAssociationStateDescriptor.associations();
        }

        @Override
        public Iterable<? extends AssociationDescriptor> manyAssociations()
        {
            return wrappedAssociationStateDescriptor.manyAssociations();
        }

        @Override
        public Iterable<? extends AssociationDescriptor> namedAssociations()
        {
            return wrappedAssociationStateDescriptor.namedAssociations();
        }

        @Override
        public PropertyDescriptor findPropertyModelByName( String name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.findPropertyModelByName( name );
        }

        @Override
        public PropertyDescriptor findPropertyModelByQualifiedName( QualifiedName name )
            throws IllegalArgumentException
        {
            return wrappedAssociationStateDescriptor.findPropertyModelByQualifiedName( name );
        }

        @Override
        public Iterable<? extends PropertyDescriptor> properties()
        {
            return Iterables.filter( PROPERTY_SPEC, wrappedAssociationStateDescriptor.properties() );
        }

        @Override
        @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
        public boolean equals( Object obj )
        {
            return wrappedAssociationStateDescriptor.equals( obj );
        }

        @Override
        public int hashCode()
        {
            return wrappedAssociationStateDescriptor.hashCode();
        }
    }

}
