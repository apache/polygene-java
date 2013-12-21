package org.qi4j.tools.shell;

public abstract class AbstractCommand
    implements Command, Comparable<Command>
{

    @Override
    public int compareTo( Command o )
    {
        return getClass().getSimpleName().compareTo( o.getClass().getSimpleName() );
    }
}
