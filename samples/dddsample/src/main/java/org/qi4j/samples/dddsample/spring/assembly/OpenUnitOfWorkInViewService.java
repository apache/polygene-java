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
package org.qi4j.samples.dddsample.spring.assembly;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;

/**
 * Ensure that unit of work still open during jsp rendering.
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( OpenUnitOfWorkInViewService.OpenUnitOfWorkMixin.class )
interface OpenUnitOfWorkInViewService
    extends WebRequestInterceptorOverride, ServiceComposite
{
    class OpenUnitOfWorkMixin
        implements WebRequestInterceptorOverride
    {
        @Structure
        private UnitOfWorkFactory uowf;

        public void preHandle( WebRequest request )
            throws Exception
        {
            uowf.newUnitOfWork();
        }

        public void postHandle( WebRequest request, ModelMap model )
            throws Exception
        {
            // Do nothing
        }

        public void afterCompletion( WebRequest request, @Optional Exception ex )
            throws Exception
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            try
            {
                if( ex != null )
                {
                    uow.discard();
                }
                else
                {
                    uow.complete();
                }
            }
            catch( Exception e )
            {
                uow.discard();
                throw e;
            }
        }
    }
}
