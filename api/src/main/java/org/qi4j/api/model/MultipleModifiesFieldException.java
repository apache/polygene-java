package org.qi4j.api.model;

public class MultipleModifiesFieldException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final Class modifierClass;

    public MultipleModifiesFieldException( String message, Class modifierClass )
    {
        super( message );
        this.modifierClass = modifierClass;
    }

    public Class getModifierClass()
    {
        return modifierClass;
    }
    
}
