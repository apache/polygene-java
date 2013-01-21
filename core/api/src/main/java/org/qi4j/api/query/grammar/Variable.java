package org.qi4j.api.query.grammar;

/**
 * Query Variable name.
 */
public class Variable
{
    private String name;

    public Variable( String name )
    {
        this.name = name;
    }

    public String variableName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
