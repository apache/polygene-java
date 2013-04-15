package org.qi4j.api.query.grammar;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.functional.Function;

/**
 * Function to get Entity ManyAssociations.
 */
public class ManyAssociationFunction<T>
    implements Function<Composite, ManyAssociation<T>>
{
    private AssociationFunction<?> traversedAssociation;
    private ManyAssociationFunction<?> traversedManyAssociation;
    private final AccessibleObject accessor;

    public ManyAssociationFunction( AssociationFunction<?> traversedAssociation,
                                    ManyAssociationFunction<?> traversedManyAssociation,
                                    AccessibleObject accessor
    )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.accessor = accessor;
    }

    public AssociationFunction<?> traversedAssociation()
    {
        return traversedAssociation;
    }

    public ManyAssociationFunction<?> traversedManyAssociation()
    {
        return traversedManyAssociation;
    }

    public AccessibleObject accessor()
    {
        return accessor;
    }

    @Override
    public ManyAssociation<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if( traversedAssociation != null )
            {
                target = traversedAssociation.map( entity ).get();
            }
            if( traversedManyAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot traverse ManyAssociations" );
            }

            CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( target );
            return ( (AssociationStateHolder) handler.state() ).manyAssociationFor( accessor );
        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public String toString()
    {
        if( traversedAssociation != null )
        {
            return traversedAssociation.toString() + "." + ( (Member) accessor ).getName();
        }
        else
        {
            return ( (Member) accessor ).getName();
        }
    }
}
