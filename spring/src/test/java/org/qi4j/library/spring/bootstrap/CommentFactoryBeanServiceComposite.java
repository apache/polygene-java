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
package org.qi4j.library.spring.bootstrap;

import org.qi4j.composite.Mixins;
import org.qi4j.injection.scope.Service;
import org.qi4j.service.ServiceComposite;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( CommentFactoryBeanServiceComposite.BadCommentFactoryBeanMixin.class )
interface CommentFactoryBeanServiceComposite extends FactoryBean, ServiceComposite
{
    class BadCommentFactoryBeanMixin
        implements FactoryBean
    {
        @Service private CommentService service;

        public final Object getObject()
            throws Exception
        {
            return service;
        }

        public final Class getObjectType()
        {
            return CommentService.class;
        }

        public final boolean isSingleton()
        {
            return true;
        }
    }
}
