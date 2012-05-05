package com.marcgrue.dcisample_b.infrastructure.conversion;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.values.PropertyNotPresentException;
import org.qi4j.library.values.Unqualified;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Set;

/**
 * EntityToDTOService
 *
 * Conversion of Entity objects to DTO's
 *
 * Value composites that extend {@link DTO} will have association properties converted recursively.
 *
 * Modification of {org.qi4j.library.conversion.values.EntityToValue}
 */
@SuppressWarnings( "unchecked" )
@Mixins( EntityToDTOService.Mixin.class )
public interface EntityToDTOService
      extends ServiceComposite
{
    <T> T convert( Class<T> valueType, Object entity );

    static abstract class Mixin
          implements EntityToDTOService
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
            if (valueDescriptor == null)
            {
                throw new NoSuchCompositeException( valueType.getName(), module.name() );
            }
            Unqualified unqualified = valueDescriptor.metaInfo( Unqualified.class );
            Set<PropertyDescriptor> properties = valueDescriptor.state().properties();
            final EntityComposite composite = (EntityComposite) entity;
            final EntityDescriptor entityDescriptor = spi.getEntityDescriptor( composite );
            final EntityState entityState = spi.getEntityState( composite );
            ValueBuilder builder = vbf.newValueBuilder( valueType );

            if (unqualified == null || !unqualified.value())
            {
                builder.withState( new StateHolder()
                {
                    public <T> Property<T> getProperty( Method propertyMethod )
                    {
                        try
                        {
                            return (Property<T>) propertyMethod.invoke( composite );
                        }
                        catch (IllegalAccessException e)
                        {
                            String message = propertyMethod + " is not present in " + entityDescriptor.type();
                            throw new PropertyNotPresentException( message,
                                                                   valueType,
                                                                   (Class<? extends EntityComposite>) composite.type() );
                        }
                        catch (InvocationTargetException e)
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
                        for (PropertyDescriptor descriptor : entityDescriptor.state().properties())
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
                for (PropertyDescriptor descriptor : entityDescriptor.state().properties())
                {
                    QualifiedName name = descriptor.qualifiedName();
                    Object value = entityState.getProperty( name );
                    setUnqualifiedProperty( builder, properties, name, value );
                }
            }
            for (AssociationDescriptor descriptor : entityDescriptor.state().associations())
            {
                QualifiedName name = descriptor.qualifiedName();

                Object propertyValue = null;

                PropertyDescriptor propertyDescriptor = findProperty( properties, name.name() );
                Class<?> dtoClass = propertyDescriptor.accessor().getDeclaringClass();
                Class propertyDtoClass = (Class) propertyDescriptor.type();

                if (DTO.class.isAssignableFrom( propertyDtoClass ))
                {
                    Method associationMethod = descriptor.accessor();
                    Association<?> association;
                    try
                    {
                        association = (Association<?>) associationMethod.invoke( composite );
                    }
                    catch (IllegalAccessException e)
                    {
                        String message = associationMethod + " is not present in " + entityDescriptor.type();
                        throw new PropertyNotPresentException( message,
                                                               valueType,
                                                               (Class<? extends EntityComposite>) composite.type() );
                    }
                    catch (InvocationTargetException e)
                    {
                        String message = "Exception in association method. Internal Error? [" + associationMethod + " / " +
                              entityDescriptor.type();
                        throw new UndeclaredThrowableException( e.getTargetException(), message );
                    }

                    Object associatedEntityInstance = association.get();

                    // Convert association value to DTO (recursively)
                    if (associatedEntityInstance != null)
                        propertyValue = convert( propertyDtoClass, associatedEntityInstance );
                }
                else
                {
                    // Set property string to entity reference
                    propertyValue = entityState.getAssociation( name ).toURI();
                }

                setUnqualifiedProperty( builder, properties, name, propertyValue );
            }
//            for (ManyAssociationDescriptor descriptor : entityDescriptor.state().manyAssociations())
            for (AssociationDescriptor descriptor : entityDescriptor.state().manyAssociations())
            {
                ArrayList<String> entityURIs = new ArrayList<String>();
                QualifiedName name = descriptor.qualifiedName();
                for (EntityReference value : entityState.getManyAssociation( name ))
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
            if (pd == null)
            {
                return;
            }
            Object prototype = builder.prototypeFor( pd.accessor().getDeclaringClass() );
            try
            {
                Property property = (Property) pd.accessor().invoke( prototype );
                property.set( value );
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }

        private PropertyDescriptor findProperty( Set<PropertyDescriptor> properties, String name )
        {
            for (PropertyDescriptor descriptor : properties)
            {
                if (descriptor.qualifiedName().name().equals( name ))
                {
                    return descriptor;
                }
            }
            return null;
        }
    }
}
