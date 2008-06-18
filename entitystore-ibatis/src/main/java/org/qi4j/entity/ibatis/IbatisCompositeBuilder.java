package org.qi4j.entity.ibatis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.ibatis.internal.CapitalizingIdentifierConverter;
import org.qi4j.property.Property;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.structure.Module;

/**
 * @autor Michael Hunger
 * @since 22.05.2008
 */
public class IbatisCompositeBuilder
{
    private final Qi4jSPI spi;
    private Module module;

    public IbatisCompositeBuilder( Qi4jSPI spi, Module module )
    {
        this.spi = spi;
        this.module = module;
    }

    public Composite createEntityComposite( final Map<String, Object> rawData, final Class<? extends Composite> compositeClass )
    {
        final CompositeBuilderFactory compositeBuilderFactory = module.compositeBuilderFactory();
        final Composite composite = compositeBuilderFactory.newComposite( compositeClass );
        final CompositeDescriptor compositeDescriptor = spi.getCompositeDescriptor( compositeClass, module );
        setCompositeProperties( composite, rawData, compositeDescriptor );
        return composite;
    }

    private void setCompositeProperties( final Composite composite, final Map<String, Object> rawData, final CompositeDescriptor compositeDescriptor )
    {
        final StateDescriptor stateDescriptor = compositeDescriptor.state();
        for( PropertyDescriptor propertyDescriptor : stateDescriptor.properties() )
        {
            final IdentifierConverter identifierConverter = new CapitalizingIdentifierConverter();
            final String qualifiedName = propertyDescriptor.qualifiedName();
            final Object value = identifierConverter.getValueFromData( rawData, qualifiedName );
            try
            {
                final Method accessor = propertyDescriptor.accessor();
                final Property prop = (Property) accessor.invoke( composite );
                // TODO if (value==null) prop.set( propertyDescriptor.defaultValue() );
                prop.set( value );
            }
            catch( InvocationTargetException e )
            {
                throw new EntityStoreException( "Error retrieving property " + qualifiedName + " of composite " + compositeDescriptor.type(), e.getTargetException() );
            }
            catch( IllegalAccessException e )
            {
                throw new EntityStoreException( "Error retrieving property " + qualifiedName + " of composite " + compositeDescriptor.type(), e );
            }
        }
    }

}
