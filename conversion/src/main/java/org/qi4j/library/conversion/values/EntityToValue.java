package org.qi4j.library.conversion.values;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
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
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
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
                builder.withState( new StateHolder()
                {
                    public <T> Property<T> getProperty( Method propertyMethod )
                    {
                        try
                        {
                            return (Property<T>) propertyMethod.invoke( composite );
                        }
                        catch( IllegalAccessException e )
                        {
                            String message = propertyMethod + " is not present in " + entityDescriptor.type();
                            throw new PropertyNotPresentException( message,
                                                                   valueType,
                                                                   (Class<? extends EntityComposite>) composite.type() );
                        }
                        catch( InvocationTargetException e )
                        {
                            String message = "Exception in property method. Internal Error? [" + propertyMethod + " / " +
                                             entityDescriptor.type();
                            throw new UndeclaredThrowableException( e.getTargetException(), message );
                        }
                    }

                    public <T> Property<T> getProperty( final QualifiedName name )
                    {
                        PropertyDescriptor info = entityDescriptor.state().getPropertyByQualifiedName( name );
                        return new ComputedPropertyInstance<T>( info )
                        {
                            @Override
                            public T get()
                            {
                                return (T) entityState.getProperty( name );
                            }
                        };
                    }

                    public <ThrowableType extends Throwable> void visitProperties( StateVisitor<ThrowableType> visitor )
                        throws ThrowableType
                    {
                        for( PropertyDescriptor descriptor : entityDescriptor.state().properties() )
                        {
                            QualifiedName name = descriptor.qualifiedName();
                            Object value = entityState.getProperty( name );
                            visitor.visitProperty( name, value );
                        }
                    }
                } );
            }
            else
            {
                for( PropertyDescriptor descriptor : entityDescriptor.state().properties() )
                {
                    QualifiedName name = descriptor.qualifiedName();
                    Object value = entityState.getProperty( name );
                    setUnqualifiedProperty( builder, properties, name, value );
                }
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
            Object prototype = builder.prototypeFor( pd.accessor().getDeclaringClass() );
            try
            {
                Property property = (Property) pd.accessor().invoke( prototype );
                property.set( value );
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            catch( InvocationTargetException e )
            {
                e.printStackTrace();
            }
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
