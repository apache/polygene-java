package org.qi4j.runtime.association;

import java.lang.reflect.Type;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.AssociationInfo;
import org.qi4j.entity.EntityComposite;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.serialization.EntityId;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected AssociationInfo associationInfo;
    protected UnitOfWorkInstance unitOfWork;

    public AbstractAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork )
    {
        this.associationInfo = associationInfo;
        this.unitOfWork = unitOfWork;
    }

    // AssociationInfo implementation
    public <T> T getAssociationInfo( Class<T> infoType )
    {
        return associationInfo.getAssociationInfo( infoType );
    }

    public String getName()
    {
        return associationInfo.getName();
    }

    public String getQualifiedName()
    {
        return associationInfo.getQualifiedName();
    }

    public Type getAssociationType()
    {
        return associationInfo.getAssociationType();
    }

    protected T getEntity( EntityId entityId )
    {
        if( entityId == null || entityId == EntityId.NULL )
        {
            return null;
        }

        Class<? extends EntityComposite> entityCompositeType = (Class<? extends EntityComposite>) unitOfWork.getModuleInstance().getModuleContext().getModuleBinding().lookupClass( entityId.getCompositeType() );
        return (T) unitOfWork.getReference( entityId.getIdentity(), entityCompositeType );
    }

    protected EntityId getEntityId( Object composite )
    {
        if( composite == null )
        {
            return EntityId.NULL;
        }

        EntityComposite entityComposite = (EntityComposite) composite;
        return new EntityId( entityComposite.identity().get(), entityComposite.getCompositeType().getName() );
    }

}
