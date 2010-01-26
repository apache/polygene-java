package org.qi4j.runtime.entity.association;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected final AssociationInfo associationInfo;
    protected final ModuleUnitOfWork unitOfWork;
    protected EntityState entityState;

    public AbstractAssociationInstance( AssociationInfo associationInfo,
                                        ModuleUnitOfWork unitOfWork,
                                        EntityState entityState
    )
    {
        this.associationInfo = associationInfo;
        this.unitOfWork = unitOfWork;
        this.entityState = entityState;
    }

    // AssociationInfo implementation

    public <T> T metaInfo( Class<T> infoType )
    {
        return associationInfo.metaInfo( infoType );
    }

    public QualifiedName qualifiedName()
    {
        return associationInfo.qualifiedName();
    }

    public Type type()
    {
        return associationInfo.type();
    }

    public boolean isImmutable()
    {
        return associationInfo.isImmutable();
    }

    public boolean isAggregated()
    {
        return associationInfo.isAggregated();
    }

    protected T getEntity( EntityReference entityId )
    {
        if( entityId == null )
        {
            return null;
        }

        return (T) unitOfWork.get( (Class<? extends Object>) type(), entityId.identity() );
    }

    protected QualifiedIdentity getEntityId( Object composite )
    {
        if( composite == null )
        {
            return null;
        }

        EntityComposite entityComposite = (EntityComposite) composite;
        return new QualifiedIdentity( entityComposite );
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
    {
        if( isImmutable() )
        {
            throw new IllegalStateException( "Association [" + qualifiedName() + "] is immutable." );
        }
    }

    protected abstract boolean isSet();
}
