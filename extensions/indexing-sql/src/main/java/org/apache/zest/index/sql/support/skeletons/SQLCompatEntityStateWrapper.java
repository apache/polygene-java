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
package org.apache.zest.index.sql.support.skeletons;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateDescriptor;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.entity.ManyAssociationState;
import org.apache.zest.spi.entity.NamedAssociationState;
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

    /* package */ static final Function<EntityState, EntityState> WRAP = SQLCompatEntityStateWrapper::new;

    private static final Predicate<PropertyDescriptor> PROPERTY_SPEC = new Predicate<PropertyDescriptor>()
    {

        @Override
        public boolean test( PropertyDescriptor propertyDescriptor )
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
        public Stream<Class<?>> mixinTypes()
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
        public Stream<Class<?>> types()
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
        public Stream<? extends AssociationDescriptor> associations()
        {
            return wrappedAssociationStateDescriptor.associations();
        }

        @Override
        public Stream<? extends AssociationDescriptor> manyAssociations()
        {
            return wrappedAssociationStateDescriptor.manyAssociations();
        }

        @Override
        public Stream<? extends AssociationDescriptor> namedAssociations()
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
        public Stream<? extends PropertyDescriptor> properties()
        {
            return wrappedAssociationStateDescriptor.properties().filter( PROPERTY_SPEC );
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
