package org.qi4j.runtime.entity.association;

import java.lang.reflect.Type;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.structure.Visibility;

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

        Class<? extends EntityComposite> entityCompositeType = (Class<? extends EntityComposite>) unitOfWork.getModuleInstance().moduleContext().getModuleBinding().findClass( entityId.getCompositeType(), Visibility.module );
        return (T) unitOfWork.getReference( entityId.getIdentity(), entityCompositeType );
    }

    protected QualifiedIdentity getEntityId( Object composite )
    {
        if( composite == null )
        {
            return QualifiedIdentity.NULL;
        }

        EntityComposite entityComposite = (EntityComposite) composite;
        return new QualifiedIdentity( entityComposite.identity().get(), entityComposite.type().getName() );
    }

}
