package org.qi4j.runtime.entity.association;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.AssociationInfo;
import org.qi4j.api.entity.association.Qualifier;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.QualifierQualifiedIdentity;

import java.lang.reflect.Type;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected final AssociationInfo associationInfo;
    protected final ModuleUnitOfWork unitOfWork;
    protected EntityState entityState;

    public AbstractAssociationInstance( AssociationInfo associationInfo, ModuleUnitOfWork unitOfWork, EntityState entityState )
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

    public void refresh( EntityState newState )
    {
        entityState = newState;
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
            throw new EntityTypeNotFoundException( entityId.type() );
        }
    }

    protected QualifiedIdentity getEntityId( Object composite )
    {
        if( composite == null )
        {
            return null;
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
        if( isImmutable() )
        {
            throw new IllegalStateException( "Association [" + qualifiedName() + "] is immutable." );
        }
    }

    protected abstract boolean isSet();
}
