package org.qi4j.tools.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface Command
{
    void execute( String[] args, BufferedReader input, PrintWriter output )
        throws HelpNeededException, IOException;

    String description();

    String name();
}
