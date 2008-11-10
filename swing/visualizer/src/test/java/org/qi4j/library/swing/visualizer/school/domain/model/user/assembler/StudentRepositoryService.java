/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.visualizer.school.domain.model.user.assembler;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.swing.visualizer.school.domain.model.user.Student;
import org.qi4j.library.swing.visualizer.school.domain.model.user.StudentId;
import org.qi4j.library.swing.visualizer.school.domain.model.user.StudentRepository;
import org.qi4j.service.ServiceComposite;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( StudentRepositoryService.StudentRepositoryMixin.class )
interface StudentRepositoryService extends StudentRepository, ServiceComposite
{
    class StudentRepositoryMixin
        implements StudentRepository
    {
        @Structure private UnitOfWorkFactory uowf;

        public final Student find( StudentId studentId )
        {
            String identity = studentId.idString();

            UnitOfWork uow = uowf.nestedUnitOfWork();
            try
            {
                Student student = uow.find( identity, Student.class );
                // Note: This is required to allow Other layer to edit student.
                completeAndContinue( uow );
                return student;
            }
            catch( EntityCompositeNotFoundException e )
            {
                uow.discard();
                return null;
            }
        }

        private void completeAndContinue( UnitOfWork uow )
        {
            try
            {
                uow.completeAndContinue();
            }
            catch( UnitOfWorkCompletionException e )
            {
                e.printStackTrace();
            }
        }
    }
}
