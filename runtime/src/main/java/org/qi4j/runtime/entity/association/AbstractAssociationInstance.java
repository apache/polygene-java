package org.qi4j.runtime.entity.association;

import java.lang.reflect.Type;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.NoSuchEntityException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{

    protected final AssociationInfo associationInfo;
    protected final UnitOfWorkInstance unitOfWork;

    public AbstractAssociationInstance( AssociationInfo associationInfo, UnitOfWorkInstance unitOfWork )
    {
        this.associationInfo = associationInfo;
        this.unitOfWork = unitOfWork;
    }

    // AssociationInfo implementation
    public <T> T metaInfo( Class<T> infoType )
    {
        return associationInfo.metaInfo( infoType );
    }

    public String name()
    {
        return associationInfo.name();
    }

    public String qualifiedName()
    {
        return associationInfo.qualifiedName();
    }

    public Type type()
    {
        return associationInfo.type();
    }

    protected T getEntity( QualifiedIdentity entityId )
    {
        if( entityId == null || entityId == QualifiedIdentity.NULL )
        {
            return null;
        }

        try
        {
            Class<? extends EntityComposite> entityCompositeType = (Class<? extends EntityComposite>) unitOfWork.module().classLoader().loadClass( entityId.type() );
            return (T) unitOfWork.getReference( entityId.identity(), entityCompositeType );
        }
        catch( ClassNotFoundException e )
        {
            throw new NoSuchEntityException( entityId.type(), unitOfWork.module().name() );
        }
    }

    protected QualifiedIdentity getEntityId( Object composite )
    {
        if( composite == null )
        {
            return QualifiedIdentity.NULL;
        }

        EntityComposite entityComposite = (EntityComposite) composite;
        return new QualifiedIdentity( entityComposite.identity().get(), entityComposite.type() );
    }
}
