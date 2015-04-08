package org.qi4j.tools.shell.model;

public class ProjectAlreadyExistsException extends Exception
{
    private final String name;

    public ProjectAlreadyExistsException( String name )
    {
        super( "Project " + Project.Support.identity( name ) + " already exists." );
        this.name = name;
    }

    public String name()
    {
        return name;
    }
}
