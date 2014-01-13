package org.qi4j.runtime.association;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.functional.Function2;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.entity.EntityInstance;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected AssociationInfo associationInfo;
    private final Function2<EntityReference, Type, Object> entityFunction;

    public AbstractAssociationInstance( AssociationInfo associationInfo,
                                        Function2<EntityReference, Type, Object> entityFunction
    )
    {
        this.associationInfo = associationInfo;
        this.entityFunction = entityFunction;
    }

    public AssociationInfo associationInfo()
    {
        return associationInfo;
    }

    public void setAssociationInfo( AssociationInfo newInfo )
    {
        this.associationInfo = newInfo;
    }

    @SuppressWarnings( "unchecked" )
    protected T getEntity( EntityReference entityId )
    {
        if( entityId == null )
        {
            return null;
        }

        return (T) entityFunction.map( entityId, associationInfo.type() );
    }

    protected EntityReference getEntityReference( Object composite )
    {
        if( composite == null )
        {
            return null;
        }

        InvocationHandler handler = Proxy.getInvocationHandler( composite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            handler = Proxy.getInvocationHandler( ( (ProxyReferenceInvocationHandler) handler ).proxy() );
        }
        EntityInstance instance = (EntityInstance) handler;
        return instance.identity();
    }

    protected void checkType( Object instance )
    {
        if( instance != null )
        {
            if( !( instance instanceof EntityComposite ) )
            {
                if( instance instanceof Proxy )
                {
                    if( Proxy.getInvocationHandler( instance ) instanceof EntityInstance )
                    {
                        return; // It's fine
                    }
                }

                throw new IllegalArgumentException( "Object must be an EntityComposite" );
            }
        }
    }

    protected void checkImmutable()
        throws IllegalStateException
    {
        if( associationInfo.isImmutable() )
        {
            throw new IllegalStateException( "Association [" + associationInfo.qualifiedName() + "] is immutable." );
        }
    }
}
