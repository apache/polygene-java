package org.qi4j.runtime.entity.association;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
    public static String getName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( idx + 1 );
    }

    public static String getDeclaringClassName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( 0, idx + 1 );
    }

    public static String getDeclaringClassName( Method accessor )
    {
        return accessor.getDeclaringClass().getName();
    }

    public static String getQualifiedName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }

    public static String getQualifiedName( Class<?> declaringClass, String name )
    {
        String className = declaringClass.getName();
        className = className.replace( '$', '&' );
        return className + ":" + name;
    }

    public static Type getAssociationType( Method accessor )
    {
        return getAssociationType( accessor.getGenericReturnType() );
    }

    public static Type getAssociationType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        Type[] interfaces = ( (Class<?>) methodReturnType ).getInterfaces();
        for( Type anInterface : interfaces )
        {
            Type associationType = getAssociationType( anInterface );
            if( associationType != null )
            {
                return associationType;
            }
        }
        return null;
    }

    /**
     * Get URI for an association.
     *
     * @param accessor accessor method
     * @return association URI
     */
    public static String toURI( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:association:" + getQualifiedName( accessor );
    }

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
        return new QualifiedIdentity( entityComposite.identity().get(), entityComposite.type().getName() );
    }
}
