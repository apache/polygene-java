/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.library.uid;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.uid.sequence.Sequencing;
import org.qi4j.library.uid.sequence.assembly.PersistingSequencingAssembler;
import org.qi4j.library.uid.sequence.assembly.TransientSequencingAssembler;
import org.qi4j.library.uid.uuid.UuidService;
import org.qi4j.library.uid.uuid.assembly.UuidServiceAssembler;

import static org.qi4j.api.common.Visibility.layer;

public class DocumentationSupport
{

    class Uuid
            implements Assembler
    {

        public void assemble( ModuleAssembly moduleAssembly )
                throws AssemblyException
        {
            // START SNIPPET: uuid-assembly
            new UuidServiceAssembler().withVisibility( layer ).assemble( moduleAssembly );
            // END SNIPPET: uuid-assembly
        }

        // START SNIPPET: uuid-usage
        @Service UuidService uuidService;

        public void doSomething()
        {
            String id1 = uuidService.generateUuid( 0 );
            // eg. 1020ECBB-098C-46E0-94DC-F78E2265EAA1-36

            String id2 = uuidService.generateUuid( 12 );
            // eg. 84E06578EAE3
        }
        // END SNIPPET: uuid-usage

    }

    class Seq
            implements Assembler
    {

        public void assemble( ModuleAssembly moduleAssembly )
                throws AssemblyException
        {
            // START SNIPPET: seq-assembly
            new TransientSequencingAssembler().withVisibility( layer ).assemble( moduleAssembly );
            // END SNIPPET: seq-assembly
        }

        // START SNIPPET: seq-usage
        @Service Sequencing sequencing;

        public void doSomething()
        {
            sequencing.currentSequenceValue(); // return 0

            sequencing.newSequenceValue(); // return 1
            sequencing.currentSequenceValue(); // return 1
        }
        // END SNIPPET: seq-usage

    }

    class Perseq
            implements Assembler
    {

        public void assemble( ModuleAssembly moduleAssembly )
                throws AssemblyException
        {
            // START SNIPPET: perseq-assembly
            new PersistingSequencingAssembler().withVisibility( layer ).assemble( moduleAssembly );
            // END SNIPPET: perseq-assembly
        }

    }

}
