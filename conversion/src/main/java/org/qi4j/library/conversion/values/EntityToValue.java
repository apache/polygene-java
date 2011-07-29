package org.qi4j.library.conversion.values;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.AssociationDescriptor;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;

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
        private Qi4j api;

        @Structure
        private Module module;

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
            final EntityDescriptor entityDescriptor = api.getEntityDescriptor( composite );
            final EntityStateHolder entityState = api.getState( composite );
            ValueBuilder builder;

            if( unqualified == null || !unqualified.value() )
            {
                builder = vbf.newValueBuilderWithState( valueType, new Function<PropertyDescriptor, Object>()
                {
                    @Override
                    public Object map( PropertyDescriptor descriptor )
                    {
                        try
                        {
                            return entityState.propertyFor( descriptor.accessor() ).get();
                        } catch( IllegalArgumentException e )
                        {
                            return null;
                        }
                    }
                } );
            }
            else
            {
                builder = vbf.newValueBuilderWithState( valueType, new Function<PropertyDescriptor, Object>()
                {
                    @Override
                    public Object map( PropertyDescriptor descriptor )
                    {
                        PropertyDescriptor propertyDescriptor = entityDescriptor.state().getPropertyByName( descriptor.qualifiedName().name() );
                        if (propertyDescriptor == null)
                            return null;

                        return entityState.propertyFor( propertyDescriptor.accessor() ).get();
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
                EntityReference value = EntityReference.getEntityReference( entityState.getAssociation( descriptor.accessor() ).get());
                setUnqualifiedProperty( builder, properties, descriptor.qualifiedName(), value.toURI() );
            }
            for( AssociationDescriptor descriptor : entityDescriptor.state().manyAssociations() )
            {
                ArrayList<String> entityURIs = new ArrayList<String>();
                for( Object value : entityState.getManyAssociation( descriptor.accessor() ) )
                {
                    entityURIs.add( EntityReference.getEntityReference( value ).toURI() );
                }
                setUnqualifiedProperty( builder, properties, descriptor.qualifiedName(), entityURIs );
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

            builder.state().propertyFor( pd.accessor() ).set( value );
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
