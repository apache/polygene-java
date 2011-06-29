package org.qi4j.library.conversion.values;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Set;

@Mixins( EntityToValue.EntityToValueMixin.class )
public interface EntityToValue
{
    <T> T convert( Class<T> valueType, Object entity );

    static class EntityToValueMixin
        implements EntityToValue
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private Qi4jSPI spi;

        @Structure
        private ModuleSPI module;

        public <T> T convert( final Class<T> valueType, Object entity )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
            if( valueDescriptor == null )
            {
                throw new NoSuchCompositeException( valueType.getName(), module.name() );
            }
            Unqualified unqualified = valueDescriptor.metaInfo( Unqualified.class );
            Set<PropertyDescriptor> properties = valueDescriptor.state().properties();
            final EntityComposite composite = (EntityComposite) entity;
            final EntityDescriptor entityDescriptor = spi.getEntityDescriptor( composite );
            final EntityState entityState = spi.getEntityState( composite );
            ValueBuilder builder = vbf.newValueBuilder( valueType );

            if( unqualified == null || !unqualified.value() )
            {
                builder.withState( new Function<PropertyInfo, Object>()
                {
                    @Override
                    public Object map( PropertyInfo propertyInfo )
                    {
                        return entityState.getProperty( propertyInfo.qualifiedName() );
                    }
                });
            }
            else
            {
                builder.withState( new Function<PropertyInfo, Object>()
                {
                    @Override
                    public Object map( PropertyInfo propertyInfo )
                    {
                        PropertyDescriptor propertyDescriptor = entityState.entityDescriptor().state().getPropertyByName( propertyInfo.qualifiedName().name() );
                        if (propertyDescriptor == null)
                            return null;

                        QualifiedName qn = propertyDescriptor.qualifiedName();
                        return entityState.getProperty( qn );
                    }
                });
/*

                for( PropertyDescriptor descriptor : entityDescriptor.state().properties() )
                {
                    QualifiedName name = descriptor.qualifiedName();
                    Object value = entityState.getProperty( name );
                    setUnqualifiedProperty( builder, properties, name, value );
                }
*/
            }


            for( AssociationDescriptor descriptor : entityDescriptor.state().associations() )
            {
                QualifiedName name = descriptor.qualifiedName();
                EntityReference value = entityState.getAssociation( name );
                setUnqualifiedProperty( builder, properties, name, value.toURI() );
            }
            for( ManyAssociationDescriptor descriptor : entityDescriptor.state().manyAssociations() )
            {
                ArrayList<String> entityURIs = new ArrayList<String>();
                QualifiedName name = descriptor.qualifiedName();
                for( EntityReference value : entityState.getManyAssociation( name ) )
                {
                    entityURIs.add( value.toURI() );
                }
                setUnqualifiedProperty( builder, properties, name, entityURIs );
            }

            return (T) builder.newInstance();
        }

        private void setUnqualifiedProperty( ValueBuilder builder, Set<PropertyDescriptor> properties, QualifiedName name, Object value )
        {
            PropertyDescriptor pd = findProperty( properties, name.name() );
            if( pd == null )
            {
                return;
            }

            builder.state().getProperty( pd.accessor() ).set( value );
        }

        private PropertyDescriptor findProperty( Set<PropertyDescriptor> properties, String name )
        {
            for( PropertyDescriptor descriptor : properties )
            {
                if( descriptor.qualifiedName().name().equals( name ) )
                {
                    return descriptor;
                }
            }
            return null;
        }
    }
}
