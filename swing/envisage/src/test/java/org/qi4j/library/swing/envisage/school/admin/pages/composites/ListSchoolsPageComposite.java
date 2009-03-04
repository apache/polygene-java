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
package org.qi4j.library.swing.envisage.school.admin.pages.composites;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.swing.envisage.school.admin.pages.ListUserPage;
import org.qi4j.library.swing.envisage.school.admin.pages.mixins.Page;
import org.qi4j.library.swing.envisage.school.domain.model.school.SchoolRepository;
import org.qi4j.library.swing.envisage.school.domain.model.school.School;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( ListSchoolsPageComposite.ListSchoolsPageMixin.class)
public interface ListSchoolsPageComposite extends Page, Composite
{
    class ListSchoolsPageMixin
        implements Page
    {
        @Service Iterable<ServiceComposite> services;
        @Service SchoolRepository schools;

        public String generateHtml()
        {
            String html = "<ul>";
            for( School school : schools.findAll() )
            {
                html+="<li>"+school.name()+"</li>";
            }
            html+="</ul>";

            return html;
        }
    }
}