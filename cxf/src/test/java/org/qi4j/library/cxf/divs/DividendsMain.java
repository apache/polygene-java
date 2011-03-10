/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.cxf.divs;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.cxf.CxfAssembler;
import org.qi4j.spi.structure.ApplicationSPI;

public class DividendsMain
{
    public static void main( String[] args )
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( ProjectedDividendsService.class ).instantiateOnStartup();
                module.values( DivStream.class, DivPoint.class );
                new CxfAssembler().assemble( module );
            }
        };
        final ApplicationSPI application = assembler.application();
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    application.passivate();
                }
                catch( Exception e )
                {
                    System.err.println( "Problem shutting down Qi4j" );
                    e.printStackTrace();
                }
            }
        });
    }
}
