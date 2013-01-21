package org.qi4j.api.query.grammar;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;

import static org.qi4j.api.util.Classes.typeOf;

/**
 * Function to get Entity Associations
 */
public class AssociationFunction<T>
    implements Function<Composite, Association<T>>
{
    private AssociationFunction<?> traversedAssociation;
    private ManyAssociationFunction<?> traversedManyAssociation;
    private final AccessibleObject accessor;

    public AssociationFunction( AssociationFunction<?> traversedAssociation,
                                ManyAssociationFunction<?> traversedManyAssociation,
                                AccessibleObject accessor
    )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.accessor = accessor;

        Type returnType = typeOf( accessor );
        if( !Association.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) ) &&
            !ManyAssociation.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) ) )
        {
            throw new QueryExpressionException( "Unsupported association type:" + returnType );
        }
        Type associationTypeAsType = GenericAssociationInfo.toAssociationType( returnType );
        if( !( associationTypeAsType instanceof Class ) )
        {
            throw new QueryExpressionException( "Unsupported association type:" + associationTypeAsType );
        }
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
    public Association<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if( traversedAssociation != null )
            {
                Association<?> association = traversedAssociation.map( entity );
                if( association == null )
                {
                    return null;
                }
                target = association.get();
            }
            else if( traversedManyAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot evaluate a ManyAssociation" );
            }

            if( target == null )
            {
                return null;
            }

            CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( target );
            return ( (AssociationStateHolder) handler.state() ).associationFor( accessor );
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
