package org.qi4j.api.query.grammar2;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInvoker;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.functional.Function;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;

/**
* TODO
*/
public class ManyAssociationFunction<T>
        implements Function<Composite, ManyAssociation<T>>
{
    private AssociationFunction<?> traversedAssociation;
    private ManyAssociationFunction<?> traversedManyAssociation;
    private final AccessibleObject accessor;

    public ManyAssociationFunction( AssociationFunction<?> traversedAssociation, ManyAssociationFunction<?> traversedManyAssociation, AccessibleObject accessor )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.accessor = accessor;
    }

    public AssociationFunction<?> getTraversedAssociation()
    {
        return traversedAssociation;
    }

    public ManyAssociationFunction<?> getTraversedManyAssociation()
    {
        return traversedManyAssociation;
    }

    public AccessibleObject getAccessor()
    {
        return accessor;
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
            return ((EntityStateHolder)handler.state()).getManyAssociation( accessor );
        } catch( IllegalArgumentException e )
        {
            throw e;
        } catch( Throwable e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public String toString()
    {
        if (traversedAssociation != null)
            return traversedAssociation.toString()+"."+ ((Member)accessor).getName();
        else
            return ((Member)accessor).getName();
    }
}
