package org.qi4j.tools.shell;

import java.io.BufferedReader;
import java.io.PrintWriter;

public interface Command
{
    void execute( String[] args, BufferedReader input, PrintWriter output )
        throws HelpNeededException;

    String description();

    String name();
}
