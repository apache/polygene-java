package org.qi4j.tools.shell.model;

public class TemplateAlreadyExistsException extends Exception
{
    private final String name;

    public TemplateAlreadyExistsException( String name )
    {
        this.name = name;
    }

    public String name()
    {
        return name;
    }
}
