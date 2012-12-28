package org.qi4j.library.conversion.values;

import org.qi4j.api.entity.EntityComposite;

public class PropertyNotPresentException extends RuntimeException
{
    private Class valueType;
    private Class<? extends EntityComposite> entityType;

    public PropertyNotPresentException( String message, Class valueType, Class<? extends EntityComposite> entityType )
    {
        super(message);
        this.valueType = valueType;
        this.entityType = entityType;
    }

    public Class valueType()
    {
        return valueType;
    }

    public Class entityType()
    {
        return entityType;
    }
}
