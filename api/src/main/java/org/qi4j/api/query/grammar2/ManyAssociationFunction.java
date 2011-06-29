package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInvoker;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.util.Function;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
* TODO
*/
public class ManyAssociationFunction<T>
        implements Function<Composite, ManyAssociation<T>>
{
    private AssociationFunction<?> traversedAssociation;
    private ManyAssociationFunction<?> traversedManyAssociation;
    private final Method method;

    public ManyAssociationFunction( AssociationFunction<?> traversedAssociation, ManyAssociationFunction<?> traversedManyAssociation, Method method )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.method = method;
    }

    public AssociationFunction<?> getTraversedAssociation()
    {
        return traversedAssociation;
    }

    public ManyAssociationFunction<?> getTraversedManyAssociation()
    {
        return traversedManyAssociation;
    }

    public Method getMethod()
    {
        return method;
    }

    @Override
    public ManyAssociation<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if (traversedAssociation != null)
                target = traversedAssociation.map( entity ).get();
            if (traversedManyAssociation != null)
                throw new IllegalArgumentException( "Cannot traverse ManyAssociations" );

            CompositeInvoker handler = (CompositeInvoker) Proxy.getInvocationHandler( target );
            return (ManyAssociation<T>) handler.invokeComposite( method, new Object[0] );
        } catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( e );
        } catch( Throwable e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public String toString()
    {
        if (traversedAssociation != null)
            return traversedAssociation.toString()+"."+method.getName();
        else
            return method.getName();
    }
}
