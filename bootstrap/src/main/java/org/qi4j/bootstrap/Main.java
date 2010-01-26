/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.bootstrap;

import org.qi4j.spi.structure.ApplicationSPI;

/**
 * This class can be used as the application class. Ensure that the ApplicationAssembler
 * is available on the classpath, and that it has been specified using the ServiceLoader
 * concept. This essentially means that you have to create a text file named:
 * "META-INF/services/org.qi4j.bootstrap.ApplicationAssembler"
 * and in it place on one line the class name of your assembler. This will allow this class
 * to find it, instantiate it, and use it as the assembler of the application.
 */
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        Energy4Java energy4Java = new Energy4Java();

        Iterable<ApplicationAssembler> assemblers = Energy4Java.getServiceLoader()
            .providers( ApplicationAssembler.class );
        for( ApplicationAssembler assembler : assemblers )
        {
            ApplicationSPI application = energy4Java.newApplication( assembler );
            application.activate();
        }
    }
}
