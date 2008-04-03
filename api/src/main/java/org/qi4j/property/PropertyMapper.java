package org.qi4j.property;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import org.qi4j.composite.Composite;

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
        if( propertyInputStream != null )
        {
            Properties configProps = new Properties();
            configProps.load( propertyInputStream );
            propertyInputStream.close();
            map( configProps, composite );
        }
    }

    public static Properties getProperties( final Composite composite )
    {
        return new Properties()
        {
            @Override public Object get( Object o )
            {
                try
                {
                    Method propertyMethod = composite.getClass().getMethod( o.toString() );
                    Property property = (Property) propertyMethod.invoke( composite );
                    return property.get();
                }
                catch( NoSuchMethodException e )
                {
                    return null;
                }
                catch( IllegalAccessException e )
                {
                    return null;
                }
                catch( InvocationTargetException e )
                {
                    return null;
                }
            }

            @Override public Object put( Object o, Object o1 )
            {
                Object oldValue = get( o );

                try
                {
                    Method propertyMethod = composite.getClass().getMethod( o.toString(), new Class[]{ Object.class } );
                    propertyMethod.invoke( composite, o1 );
                }
                catch( NoSuchMethodException e )
                {
                    e.printStackTrace();
                }
                catch( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
                catch( InvocationTargetException e )
                {
                    e.printStackTrace();
                }

                return oldValue;
            }
        };
    }
}
