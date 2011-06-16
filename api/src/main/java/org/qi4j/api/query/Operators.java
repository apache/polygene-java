package org.qi4j.api.query;

import com.sun.java.browser.net.ProxyInfo;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

import java.lang.reflect.*;

import static org.qi4j.api.util.Iterables.iterable;
import static org.qi4j.api.util.Iterables.prepend;

/**
 * TODO
 */
public class Operators
{
    public interface Value<T>
    {
        T value();
    }

    public static <T> Value<T> constant( final T value)
    {
        return new Value<T>()
        {
            @Override
            public T value()
            {
                return value;
            }

            @Override
            public String toString()
            {
                return value.toString();
            }
        };
    }

    public static <T> Value<T> var(String name)
    {
        return null;
//        return new Value<T>()
//        {
//            return null; // TODO
//        }
    }

    public static <T> T template( Class<T> clazz )
    {
        return clazz.cast( Proxy.newProxyInstance( clazz.getClassLoader(), new Class[]{clazz}, new TemplateHandler<Object>(null) ) );
    }

    public static <T> Function<Entity, Property<T>> property( Property<T> property )
    {
        return ((PropertyReferenceHandler<T>)Proxy.getInvocationHandler( property )).getEntityProperty();
    }

    public static <T> Specification<Entity> eq( Property<T> property, Value<T> value )
    {
        return new EqSpecification<T>(  property( property ), value );
    }

    public static Specification<Entity> and(Specification<Entity> left, Specification<Entity> right, Specification<Entity>... optionalRight)
    {
        return new AndSpecification(prepend( left, prepend( right, iterable( optionalRight ) ) ));
    }

    public static BooleanExpression expression( final Specification<Entity> entitySpecification)
    {
        return new BooleanExpression()
        {
            @Override
            public boolean eval( Object target )
            {
                return entitySpecification.satisfiedBy( (Entity) target );
            }
        };
    }

    public interface PropertyReference
    {
        <T> Function<Entity, Property<T>> reference();
    }

    public static class EqSpecification<T>
            implements Specification<Entity>
    {
        private final Function<Entity, Property<T>> property;
        private final Value<T> value;

        public EqSpecification( Function<Entity, Property<T>> property, Value<T> value )
        {
            this.property = property;
            this.value = value;
        }

        public Function<Entity, Property<T>> getProperty()
        {
            return property;
        }

        public Value<T> getValue()
        {
            return value;
        }

        @Override
        public boolean satisfiedBy( Entity item )
        {
            return property.map( item ).get().equals( value.value() );
        }

        @Override
        public String toString()
        {
            return property.toString() + "=" + value.toString();
        }
    }

    public static class AndSpecification implements Specification<Entity>
    {
        private Iterable<Specification<Entity>> operands;

        public AndSpecification( Iterable<Specification<Entity>> operands)
        {
            this.operands = operands;
        }

        public Iterable<Specification<Entity>> getOperands()
        {
            return operands;
        }

        @Override
        public boolean satisfiedBy( Entity item )
        {
            return Specifications.and( operands ).satisfiedBy( item );
        }
    }

    private static class TemplateHandler<T>
            implements InvocationHandler
    {
        private Function<Entity, Property<T>> entityProperty;

        private TemplateHandler(Function<Entity, Property<T>> entityProperty )
        {
            this.entityProperty = entityProperty;
        }

        @Override
        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                if ( entityProperty == null)
                    return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()}, new PropertyReferenceHandler( new EntityPropertyFunction( method ) ) );
                else
                    return Proxy.newProxyInstance( method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()}, new PropertyReferenceHandler( new PropertyPropertyFunction( entityProperty, method ) ) );
            }

            return null;
        }
    }

    private static class PropertyReferenceHandler<T>
            implements InvocationHandler
    {
        private Function<Entity, Property<T>> entityProperty;

        public PropertyReferenceHandler( Function<Entity, Property<T>> entityProperty )
        {
            this.entityProperty = entityProperty;
        }

        public Function<Entity, Property<T>> getEntityProperty()
        {
            return entityProperty;
        }

        @Override
        public Object invoke( Object o, final Method method, Object[] objects ) throws Throwable
        {
            if( method.equals( Property.class.getMethod( "get" ) ) )
            {
                Type propertyType = GenericPropertyInfo.getPropertyType( ((EntityPropertyFunction) entityProperty).getMethod() );
                if( propertyType.getClass().equals( Class.class ) )
                    return Proxy.newProxyInstance( method.getDeclaringClass().getClassLoader(),
                            new Class[]{(Class) propertyType, PropertyReference.class},
                            new TemplateHandler( entityProperty ));
            }

            return null;
        }
    }

    public static class EntityPropertyFunction<T>
            implements Function<Entity, Property<T>>
    {
        private final Method method;

        public EntityPropertyFunction( Method method )
        {
            this.method = method;
        }

        public Method getMethod()
        {
            return method;
        }

        @Override
        public Property<T> map( Entity entity )
        {
            try
            {
                return (Property<T>) method.invoke( entity );
            } catch( IllegalAccessException e )
            {
                throw new IllegalArgumentException( e );
            } catch( InvocationTargetException e )
            {
                throw new IllegalArgumentException( e );
            }
        }

        @Override
        public String toString()
        {
            return method.getName();
        }
    }

    public static class PropertyPropertyFunction<T>
            implements Function<Entity, Property<T>>
    {
        private Function<Entity, Property<T>> parentProperty;
        private final Method method;

        public PropertyPropertyFunction( Function<Entity, Property<T>> parentProperty, Method method )
        {
            this.parentProperty = parentProperty;
            this.method = method;
        }

        public Method getMethod()
        {
            return method;
        }

        @Override
        public Property<T> map( Entity entity )
        {
            try
            {
                return (Property<T>) method.invoke( parentProperty.map( entity ).get() );
            } catch( IllegalAccessException e )
            {
                throw new IllegalArgumentException( e );
            } catch( InvocationTargetException e )
            {
                throw new IllegalArgumentException( e );
            }
        }

        @Override
        public String toString()
        {
            return parentProperty + "." + method.getName();
        }
    }
}
