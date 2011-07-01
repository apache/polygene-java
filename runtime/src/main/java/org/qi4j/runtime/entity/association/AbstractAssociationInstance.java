package org.qi4j.runtime.entity.association;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.AssociationDescriptor;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected final AssociationDescriptor associationDescriptor;
    protected final ModuleUnitOfWork unitOfWork;
    protected EntityState entityState;

    public AbstractAssociationInstance( AssociationDescriptor associationDescriptor,
                                        ModuleUnitOfWork unitOfWork,
                                        EntityState entityState
    )
    {
        this.associationDescriptor = associationDescriptor;
        this.unitOfWork = unitOfWork;
        this.entityState = entityState;
    }

    public AssociationDescriptor getAssociationDescriptor()
    {
        return associationDescriptor;
    }

    protected T getEntity( EntityReference entityId )
    {
        if( entityId == null )
        {
            return null;
        }

        return (T) unitOfWork.get( (Class<? extends Object>) associationDescriptor.type(), entityId.identity() );
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

    protected void checkImmutable() throws IllegalStateException
    {
        if( associationDescriptor.isImmutable() )
        {
            throw new IllegalStateException( "Association [" + associationDescriptor.qualifiedName() + "] is immutable." );
        }
    }

    protected abstract boolean isSet();
}
