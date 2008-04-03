package org.qi4j.library.framework.properties;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import org.qi4j.composite.Composite;
import org.qi4j.property.Property;

/**
 * Transfer properties to Composite properties
 */
public class PropertyMapper
{
    public static void map( Properties props, Composite composite )
    {
        for( Map.Entry<Object, Object> objectObjectEntry : props.entrySet() )
        {
            try
            {
                Method propertyMethod = composite.getClass().getMethod( objectObjectEntry.getKey().toString() );
                Property<Object> property = (Property<Object>) propertyMethod.invoke( composite );
                property.set( objectObjectEntry.getValue() );
            }
            catch( NoSuchMethodException e )
            {
                throw new IllegalArgumentException( "Could not find any property named " + objectObjectEntry.getKey() );
            }
            catch( IllegalAccessException e )
            {
                throw (IllegalArgumentException) new IllegalArgumentException( "Could not populate property named " + objectObjectEntry.getKey() ).initCause( e );
            }
            catch( InvocationTargetException e )
            {
                throw (IllegalArgumentException) new IllegalArgumentException( "Could not populate property named " + objectObjectEntry.getKey() ).initCause( e );
            }
        }
    }

    public static void map( InputStream propertyInputStream, Composite composite )
        throws IOException
    {
        Properties configProps = new Properties();
        configProps.load( propertyInputStream );
        propertyInputStream.close();
        map( configProps, composite );
    }
}
