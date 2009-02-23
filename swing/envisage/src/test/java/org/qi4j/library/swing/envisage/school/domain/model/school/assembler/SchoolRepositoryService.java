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
package org.qi4j.library.swing.envisage.school.domain.model.school.assembler;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.library.swing.envisage.school.domain.model.school.School;
import org.qi4j.library.swing.envisage.school.domain.model.school.SchoolRepository;
import org.qi4j.library.swing.envisage.school.domain.model.school.SchoolId;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( SchoolRepositoryService.SchoolRepositoryMixin.class )
interface SchoolRepositoryService extends SchoolRepository, ServiceComposite
{
    class SchoolRepositoryMixin
        implements SchoolRepository
    {
        @Structure private UnitOfWorkFactory uowf;

        public final Query<School> findAll()
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();

            try
            {
                QueryBuilderFactory qbf = uow.queryBuilderFactory();
                return qbf.newQueryBuilder( School.class ).newQuery();
            }
            finally
            {
                uow.pause();
            }
        }

        public final School find( SchoolId schoolId )
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();
            try
            {
                School school = uow.find( schoolId.idString(), School.class );
                uow.apply();
                return school;
            }
            catch( NoSuchEntityException e )
            {
                uow.discard();
                return null;
            }
            catch( UnitOfWorkCompletionException e )
            {
                // Shouldn't happened
                return null;
            }
        }
    }
}