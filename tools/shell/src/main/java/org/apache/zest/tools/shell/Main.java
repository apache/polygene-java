/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.tools.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import org.apache.zest.tools.shell.create.CreateProject;
import org.apache.zest.tools.shell.help.HelpCommand;

public class Main
{
    private Map<String, Command> commands = new TreeMap<>();

    public static void main( String[] args )
    {
        new Main().run( args );
    }

    private Main()
    {
        addCommand( new HelpCommand() );
        addCommand( new CreateProject() );
    }

    private void addCommand( Command command )
    {
        commands.put( command.name(), command );
    }

    private void run( String[] args )
    {
        if( !contains( args, "-q" ) )
        {
            System.out.println( "Zest - Classes are Dead. Long Live Interfaces!" );
            System.out.println( "----------------------------------------------\n" );
        }
        if( args.length == 0 )
        {
            commands.get( "help" ).execute( args, input(), error() );
        }
        else
        {
            try
            {
                executeCommand( args );
            }
            catch( HelpNeededException e )
            {
                commands.get( "help" ).execute( args, input(), error() );
            }
        }
    }

    private void executeCommand( String[] args )
    {
        Command command = commands.get( args[ 0 ] );
        if( command == null )
        {
            System.err.println( "Command " + args[ 0 ] + " is unknown." );
            System.err.println( "" );
            commands.get( "help" ).execute( args, input(), error() );
        }
        else
        {
            command.execute( args, input(), output() );
        }
    }

    private boolean contains( String[] args, String s )
    {
        for( String arg : args )
        {
            if( s.equals( arg ) )
            {
                return true;
            }
        }
        return false;
    }

    private PrintWriter error()
    {
        return new PrintWriter( System.err );
    }

    private PrintWriter output()
    {
        return new PrintWriter( System.out );
    }

    private BufferedReader input()
    {
        return new BufferedReader( new InputStreamReader( System.in ) );
    }
}
