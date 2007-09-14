package org.qi4j.api.persistence.impl;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.persistence.Query;

/**
 * TODO
 */
public class WhereQuery<T> extends QueryDecorator<T>
{
    private List<WhereConstraint> constraints = new ArrayList<WhereConstraint>();

    public WhereQuery( Query<T> query )
    {
        super( query );
    }

    public void resultType( Class mixinType )
    {
        query.resultType( mixinType );
    }

    public <K> K where( Class<K> mixinType )
    {
        InvocationHandler ih = new WhereInvocationHandler( Is.EQUAL );
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, ih ) );
    }

    public <K> K where( Class<K> mixinType, Is comparisonOperator )
    {
        InvocationHandler ih = new WhereInvocationHandler( comparisonOperator );
        return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, ih ) );
    }

    public Iterable<T> prepare()
    {
        if( constraints.isEmpty() )
        {
            return query.prepare();
        }
        else
        {
            return new WhereIterable<T>( query.prepare(), constraints );
        }
    }

    public T find()
    {
        Iterator<T> iterator = prepare().iterator();
        if( iterator.hasNext() )
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }

    public List<WhereConstraint> getConstraints()
    {
        return constraints;
    }

    private class WhereInvocationHandler
        implements InvocationHandler
    {
        private Is comparisonOperator;

        private WhereInvocationHandler( Is comparisonOperator )
        {
            this.comparisonOperator = comparisonOperator;
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( method.getName().startsWith( "set" ) )
            {
                // Find get method
                BeanInfo info = Introspector.getBeanInfo( method.getDeclaringClass() );
                PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
                for( PropertyDescriptor descriptor : descriptors )
                {
                    Method writeMethod = descriptor.getWriteMethod();
                    if( writeMethod != null && writeMethod.equals( method ) )
                    {
                        constraints.add( new WherePropertyConstraint( descriptor.getReadMethod(), objects[ 0 ], comparisonOperator ) );
                        break;
                    }
                }
            }

            return null;
        }
    }
}