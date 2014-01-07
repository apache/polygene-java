/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage;

import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;

/**
 * Start Envisage with a specified application assembler. Specify assembler class
 * as first parameter.
 */
public class Main
{
    public static void main( String[] args )
        throws ClassNotFoundException, IllegalAccessException, InstantiationException, AssemblyException
    {
        String applicationAssemblerName = args[0];
        System.out.println( "Assembler:" + applicationAssemblerName );
        Class applicationAssemblerClass = Class.forName( applicationAssemblerName );
        ApplicationAssembler assembler = (ApplicationAssembler) applicationAssemblerClass.newInstance();

        Energy4Java energy4Java = new Energy4Java();

        ApplicationDescriptor application = energy4Java.newApplicationModel( assembler );

        new Envisage().run( application );
    }
}
