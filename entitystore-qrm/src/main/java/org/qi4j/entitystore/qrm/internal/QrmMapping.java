package org.qi4j.entitystore.qrm.internal;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.spi.entity.EntityDescriptor;

/**
 * User: alex
 */
public class QrmMapping
{

    private List<QrmProperty> properties = new ArrayList<QrmProperty>();

    private EntityDescriptor entityDescriptor = null;

    public QrmMapping( EntityDescriptor entityDescriptor )
    {
        this.entityDescriptor = entityDescriptor;

        properties.add( new QrmProperty.Default( "updatedOn", "timestamp", false ) );
        properties.add( new QrmProperty.Default( "createdOn", "timestamp", false ) );
    }

    public Iterable<QrmProperty> properties()
    {
        return properties;
    }

    public void addProperty( String name, String hibernateType, boolean nullable )
    {
        properties.add( new QrmProperty.Default( name, hibernateType, nullable ) );
    }

    public void addIdentity( String name, String column, String hibernateType )
    {
        properties.add( new QrmProperty.Default( name, column, hibernateType, false, true ) );
    }

    public QrmProperty identity()
    {
        for( QrmProperty property : properties )
        {
            if( property.isIdentity() )
            {
                return property;
            }
        }

        return null;
    }

    public QrmProperty updatedOn()
    {
        for( QrmProperty property : properties )
        {
            if( property.name().equals( "updatedOn" ) )
            {
                return property;
            }
        }

        return null;
    }

    public QrmProperty createdOn()
    {
        for( QrmProperty property : properties )
        {
            if( property.name().equals( "createdOn" ) )
            {
                return property;
            }
        }

        return null;
    }

    public EntityDescriptor getEntityDescriptor()
    {
        return entityDescriptor;
    }
}
