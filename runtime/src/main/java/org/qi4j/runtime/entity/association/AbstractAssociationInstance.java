package org.qi4j.runtime.entity.association;

import java.lang.reflect.Type;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.NoSuchEntityException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.entity.association.AssociationInfo;
import org.qi4j.entity.association.Qualifier;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.QualifierQualifiedIdentity;
import org.qi4j.composite.Immutable;

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

    public boolean isImmutable()
    {
        return associationInfo.isImmutable();
    }

    protected T getEntity( QualifiedIdentity entityId )
    {
        if( entityId == null || entityId == QualifiedIdentity.NULL )
        {
            return null;
        }

        try
        {
            if( entityId instanceof QualifierQualifiedIdentity )
            {
                QualifierQualifiedIdentity qualifierId = (QualifierQualifiedIdentity) entityId;
                Class<? extends EntityComposite> entityCompositeType = (Class<? extends EntityComposite>) unitOfWork.module().classLoader().loadClass( entityId.type() );
                EntityComposite association = unitOfWork.getReference( entityId.identity(), entityCompositeType );

                Class<? extends EntityComposite> roleCompositeType = (Class<? extends EntityComposite>) unitOfWork.module().classLoader().loadClass( qualifierId.role().type() );
                EntityComposite role = unitOfWork.getReference( qualifierId.role().identity(), roleCompositeType );

                return (T) Qualifier.qualifier( association, role );
            }
            else
            {
                Class<? extends EntityComposite> entityCompositeType = (Class<? extends EntityComposite>) unitOfWork.module().classLoader().loadClass( entityId.type() );
                return (T) unitOfWork.getReference( entityId.identity(), entityCompositeType );
            }
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

        if( composite instanceof Qualifier )
        {
            Qualifier qualifier = (Qualifier) composite;
            return new QualifierQualifiedIdentity( qualifier );
        }
        else
        {
            EntityComposite entityComposite = (EntityComposite) composite;
            return new QualifiedIdentity( entityComposite.identity().get(), entityComposite.type() );
        }
    }


    protected void checkType( Object instance )
    {
        if( instance != null )
        {
            if( !( instance instanceof EntityComposite || instance instanceof Qualifier ) )
            {
                throw new IllegalArgumentException( "Object must be an EntityComposite or Qualifier" );
            }
        }
    }

    protected void checkImmutable()
    {
        if( !isImmutable() )
        {
            return;
        }
        if( !isSet() )
        {
            return;
        }
        throw new IllegalStateException( "Association " + this + " is immutable." );
    }

    protected abstract boolean isSet();
}
