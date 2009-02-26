/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.envisage.school;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.swing.envisage.Envisage;
import org.qi4j.library.swing.envisage.school.admin.AdminAssembler;
import org.qi4j.library.swing.envisage.school.domain.model.person.assembler.PersonModelAssembler;
import org.qi4j.library.swing.envisage.school.domain.model.school.assembler.SchoolModelAssembler;
import org.qi4j.library.swing.envisage.school.infrastructure.mail.MailServiceAssembler;
import org.qi4j.library.swing.envisage.school.infrastructure.persistence.PersistenceAssembler;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class EnvisageSchoolSample
{
    public static void main( String[] args ) throws Exception
    {
        EnvisageSchoolSample sample = new EnvisageSchoolSample();
        sample.runSample();
    }

    private void runSample() throws Exception
    {
        Energy4Java energy4Java = new Energy4Java(); 

        Application application = energy4Java.newApplication( new SchoolAssembler());
        application.activate();

        new Envisage().run( energy4Java, application );
    }
}
