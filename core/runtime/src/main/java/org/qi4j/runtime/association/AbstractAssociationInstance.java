package org.qi4j.runtime.association;

import java.lang.reflect.Type;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.functional.Function2;

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

        return new EntityReference( ( (Identity) composite ).identity().get() );
    }

    protected void checkType( Object instance )
    {

        if( instance instanceof Identity || instance == null )
        {
            return;
        }
        throw new IllegalArgumentException( "Object must be a subtype of org.qi4j.api.identity.Identity" );
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
