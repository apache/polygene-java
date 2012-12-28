/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.library.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class DocumentationSupport
        implements Assembler
{

    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( EmbeddedDatabaseService.class );
    }
    // END SNIPPET: assembly

    // START SNIPPET: neo4j
    @Service EmbeddedDatabaseService neo4jService;

    public void doSomething()
    {
        GraphDatabaseService db = neo4jService.database();
        // END SNIPPET: neo4j
        // START SNIPPET: neo4j
    }
    // END SNIPPET: neo4j

}
