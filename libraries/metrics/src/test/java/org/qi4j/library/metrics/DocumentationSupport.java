/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.library.metrics;


import java.util.List;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class DocumentationSupport
{
// START SNIPPET: capture
    public interface Router
    {
        @TimingCapture
        List<Coordinate> route( String source, String destination );
    }

    public class RouterAlgorithm1
        implements Router
    {
        @Override
        public List<Coordinate> route( String source, String destination )
        {
// END SNIPPET: capture
            return null;
// START SNIPPET: capture
        }
    }

    public class RouterAlgorithm2
        implements Router
    {
        @Override
        public List<Coordinate> route( String source, String destination )
        {
// END SNIPPET: capture
            return null;
// START SNIPPET: capture
        }

// END SNIPPET: capture
        public class MyAsembler implements Assembler
        {
// START SNIPPET: capture
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( Router.class ).identifiedBy( "router1" ).withMixins( RouterAlgorithm1.class );
                module.addServices( Router.class ).identifiedBy( "router2" ).withMixins( RouterAlgorithm2.class );
// END SNIPPET: capture
// START SNIPPET: capture
            }
        }
    }
// END SNIPPET: capture

    public class Coordinate
    {
    }
}
