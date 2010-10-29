package org.qi4j.api.composite;

import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.DateFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Transfer properties to Composite properties
 */
public final class PropertyMapper
{

    private final static Map<Type, MappingStrategy> STRATEGY;

    static
    {
        STRATEGY = new HashMap<Type, MappingStrategy>();
        STRATEGY.put( Integer.class, new IntegerMapper() );
        STRATEGY.put( Long.class, new LongMapper() );
        STRATEGY.put( Short.class, new ShortMapper() );
        STRATEGY.put( Byte.class, new ByteMapper() );
        STRATEGY.put( String.class, new StringMapper() );
        STRATEGY.put( Character.class, new CharMapper() );
        STRATEGY.put( Float.class, new FloatMapper() );
        STRATEGY.put( Double.class, new DoubleMapper() );
        STRATEGY.put( Date.class, new DateMapper() );
        STRATEGY.put( Boolean.class, new BooleanMapper() );
        STRATEGY.put( BigDecimal.class, new BigDecimalMapper() );
        STRATEGY.put( BigInteger.class, new BigIntegerMapper() );
        STRATEGY.put( Enum.class, new EnumMapper() );
        STRATEGY.put( Array.class, new ArrayMapper() );
        STRATEGY.put( Map.class, new MapMapper() );
        STRATEGY.put( List.class, new ListMapper() );
        STRATEGY.put( Set.class, new SetMapper() );
    }

    /**
     * Populate the Composite with properties from the given properties object.
     *
     * @param props     properties object
     * @param composite the composite instance
     * @throws IllegalArgumentException if properties could not be transferred to composite
     */
    public static void map( Properties props, Composite composite )
            throws IllegalArgumentException
    {
        for (Map.Entry<Object, Object> objectObjectEntry : props.entrySet())
        {
            try
            {
                Method propertyMethod = composite.getClass()
                        .getInterfaces()[0].getMethod( objectObjectEntry.getKey().toString() );
                propertyMethod.setAccessible( true );
                Object value = objectObjectEntry.getValue();
                Type propertyType = GenericPropertyInfo.getPropertyType( propertyMethod );

                value = mapToType( propertyType, value.toString() );

                @SuppressWarnings("unchecked")
                Property<Object> property = (Property<Object>) propertyMethod.invoke( composite );
                property.set( value );
            }
            catch (NoSuchMethodException e)
            {
                throw new IllegalArgumentException( "Could not find any property named " + objectObjectEntry.getKey() );
            }
            catch (IllegalAccessException e)
            {
                //noinspection ThrowableInstanceNeverThrown
                throw (IllegalArgumentException) new IllegalArgumentException( "Could not populate property named " + objectObjectEntry
                        .getKey() ).initCause( e );
            }
            catch (InvocationTargetException e)
            {
                //noinspection ThrowableInstanceNeverThrown
                String message = "Could not populate property named " + objectObjectEntry.getKey();
                IllegalArgumentException exception = new IllegalArgumentException( message );
                exception.initCause( e );
                throw exception;
            }
        }
    }

    private static Object mapToType( Type propertyType, Object value )
    {
        final String stringValue = value.toString();
        MappingStrategy strategy;
        if (propertyType instanceof Class)
        {
            Class type = (Class) propertyType;
            if (type.isArray())
            {
                strategy = STRATEGY.get( Array.class );
            } else if (Enum.class.isAssignableFrom( Classes.getRawClass( propertyType ) ))
            {
                strategy = STRATEGY.get( Enum.class );
            } else
            {
                strategy = STRATEGY.get( type );
            }
        } else if (propertyType instanceof ParameterizedType)
        {
            ParameterizedType type = ((ParameterizedType) propertyType);

            if (type.getRawType() instanceof Class)
            {
                Class clazz = (Class) type.getRawType();
                if (List.class.isAssignableFrom( clazz ))
                {
                    strategy = STRATEGY.get( List.class );
                } else if (Set.class.isAssignableFrom( clazz ))
                {
                    strategy = STRATEGY.get( Set.class );
                } else if (Map.class.isAssignableFrom( clazz ))
                {
                    strategy = STRATEGY.get( Map.class );
                } else
                {
                    throw new IllegalArgumentException( propertyType + " is not supported." );
                }
            } else
            {
                throw new IllegalArgumentException( propertyType + " is not supported." );
            }
        } else
        {
            throw new IllegalArgumentException( propertyType + " is not supported." );
        }

        if (strategy == null)
            throw new IllegalArgumentException( propertyType + " is not supported." );

        return strategy.map( propertyType, stringValue );
    }

    /**
     * Load a Properties object from the given stream, close it, and then populate
     * the Composite with the properties.
     *
     * @param propertyInputStream properties input stream
     * @param composite           the instance
     * @throws IOException if the stream could not be read
     */

    public static void map( InputStream propertyInputStream, Composite composite )
            throws IOException
    {
        if (propertyInputStream != null)
        {
            Properties configProps = new Properties();
            try
            {
                configProps.load( propertyInputStream );
            }
            finally
            {
                propertyInputStream.close();
            }
            map( configProps, composite );
        }
    }

    /**
     * Create Properties object which is backed by the given Composite.
     *
     * @param composite the instance
     * @return properties instance
     */
    public static Properties getProperties( final Composite composite )
    {
        return new Properties()
        {
            private static final long serialVersionUID = 3550125427530538865L;

            @Override
            public Object get( Object o )
            {
                try
                {
                    Method propertyMethod = composite.getClass().getMethod( o.toString() );
                    Property<?> property = (Property<?>) propertyMethod.invoke( composite );
                    return property.get();
                }
                catch (NoSuchMethodException e)
                {
                    return null;
                }
                catch (IllegalAccessException e)
                {
                    return null;
                }
                catch (InvocationTargetException e)
                {
                    return null;
                }
            }

            @Override
            public Object put( Object o, Object o1 )
            {
                Object oldValue = get( o );

                try
                {
                    Method propertyMethod = composite.getClass().getMethod( o.toString(), Object.class );
                    propertyMethod.invoke( composite, o1 );
                }
                catch (NoSuchMethodException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                    e.printStackTrace();
                }

                return oldValue;
            }
        };
    }

    private static void tokenize( String valueString, boolean mapSyntax, TokenizerCallback callback )
    {
        char[] data = valueString.toCharArray();

        int oldPos = 0;
        for (int pos = 0; pos < data.length; pos++)
        {
            char ch = data[pos];
            if (ch == '\"')
            {
                pos = resolveQuotes( valueString, callback, data, pos, '\"' );
                oldPos = pos;
            }
            if (ch == '\'')
            {
                pos = resolveQuotes( valueString, callback, data, pos, '\'' );
                oldPos = pos;
            }
            if (ch == ',' || (mapSyntax && ch == ':'))
            {
                String token = new String( data, oldPos, pos - oldPos );
                callback.token( token );
                oldPos = pos + 1;
            }
        }
        String token = new String( data, oldPos, data.length - oldPos );
        callback.token( token );
    }

    private static int resolveQuotes( String valueString,
                                      TokenizerCallback callback,
                                      char[] data,
                                      int pos, char quote
    )
    {
        boolean found = false;
        for (int j = pos + 1; j < data.length; j++)
        {
            if (!found)
            {
                if (data[j] == quote)
                {
                    String token = new String( data, pos + 1, j - pos - 1 );
                    callback.token( token );
                    found = true;
                }
            } else
            {
                if (data[j] == ',')
                {
                    return j + 1;
                }
            }
        }
        if (!found)
        {
            throw new IllegalArgumentException( "String is not quoted correctly: " + valueString );
        }
        return data.length;
    }

    private interface TokenizerCallback
    {
        void token( String token );
    }

    private interface MappingStrategy
    {
        Object map( Type type, String value );
    }

    private static class StringMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return value;
        }
    }

    private static class IntegerMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Integer( value.trim() );
        }
    }

    private static class FloatMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Float( value.trim() );
        }
    }

    private static class DoubleMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Double( value.trim() );
        }
    }

    private static class LongMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Long( value.trim() );
        }
    }

    private static class ShortMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Short( value.trim() );
        }
    }

    private static class ByteMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new Byte( value.trim() );
        }
    }

    private static class CharMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return value.trim().charAt( 0 );
        }
    }

    private static class BigDecimalMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new BigDecimal( value.trim() );
        }
    }

    private static class BigIntegerMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return new BigInteger( value.trim() );
        }
    }

    private static class EnumMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return Enum.valueOf( (Class<Enum>) type, value );
        }
    }

    private static class DateMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return DateFunctions.fromString( value.trim() );
        }
    }

    private static class ArrayMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            final Class arrayType = ((Class) type).getComponentType();
            final ArrayList result = new ArrayList();
            tokenize( value, false, new TokenizerCallback()
            {
                public void token( String token )
                {
                    result.add( mapToType( arrayType, token ) );
                }
            } );
            return result.toArray( (Object[]) Array.newInstance( arrayType, result.size() ) );
        }
    }

    private static class BooleanMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            return Boolean.valueOf( value.trim() );
        }
    }

    private static class ListMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            final Type dataType = ((ParameterizedType) type).getActualTypeArguments()[0];
            final Collection result = new ArrayList();
            tokenize( value, false, new TokenizerCallback()
            {
                public void token( String token )
                {
                    result.add( mapToType( dataType, token ) );
                }
            } );
            return result;
        }
    }

    private static class SetMapper
            implements MappingStrategy
    {
        public Object map( Type type, String value )
        {
            final Type dataType = ((ParameterizedType) type).getActualTypeArguments()[0];
            final Collection result = new HashSet();
            tokenize( value, false, new TokenizerCallback()
            {
                public void token( String token )
                {
                    result.add( mapToType( dataType, token ) );
                }
            } );
            return result;
        }
    }

    private static class MapMapper
            implements MappingStrategy
    {
        public Object map( Type generictype, String value )
        {
            ParameterizedType type = (ParameterizedType) generictype;
            final Type keyType = type.getActualTypeArguments()[0];
            final Type valueType = type.getActualTypeArguments()[0];
            final Map result = new HashMap();
            tokenize( value, true, new TokenizerCallback()
            {
                boolean keyArrivingNext = true;
                String key;

                public void token( String token )
                {
                    if (keyArrivingNext)
                    {
                        key = token;
                        keyArrivingNext = false;
                    } else
                    {
                        result.put( mapToType( keyType, key ), mapToType( valueType, token ) );
                        keyArrivingNext = true;
                    }
                }
            } );
            return result;
        }
    }
}
