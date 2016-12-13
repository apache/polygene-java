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
package org.apache.polygene.tools.shell.create;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.polygene.tools.shell.AbstractCommand;
import org.apache.polygene.tools.shell.FileUtils;
import org.apache.polygene.tools.shell.HelpNeededException;
import org.apache.polygene.tools.shell.create.project.ProjectCreator;

public class CreateProject extends AbstractCommand
{

    @Override
    public void execute( String[] args, BufferedReader input, PrintWriter output )
        throws HelpNeededException
    {
        if( args.length != 4 )
        {
            throw new HelpNeededException();
        }
        String template = args[ 1 ];
        String projectName = args[ 2 ];
        String rootPackage = args[ 3 ];
        File projectDir = FileUtils.createDir( projectName );
        Map<String, String> props = FileUtils.readTemplateProperties( template );
        if( props == null )
        {
            System.err.println( "Project Template " + template + " does not exist. \n\n" );
            throw new HelpNeededException();
        }
        props.put( "project.dir", projectDir.getAbsolutePath() );
        props.put( "project.name", projectName);
        props.put( "template", template);
        props.put( "root.package", rootPackage);
        String classname = props.get( "creator.class" );
        try
        {
            ProjectCreator creator = (ProjectCreator) Class.forName( classname ).newInstance();
            creator.create( args[ 0 ], projectDir, props );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( "Unable to create the Project Creator." );
        }
    }

    @Override
    public String description()
    {
        return "type name package\tCreates a new skeletal project in directory [name].";
    }

    @Override
    public String name()
    {
        return "create-project";
    }
}
