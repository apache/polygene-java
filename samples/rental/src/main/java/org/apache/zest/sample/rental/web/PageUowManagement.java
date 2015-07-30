/*
 * Copyright 2009 Niclas Hedhman.
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

package org.apache.zest.sample.rental.web;

import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;

public abstract class PageUowManagement
    extends ConcernOf<Page>
    implements Page
{
    @Structure
    private UnitOfWorkFactory uowf;

    public void render( QuikitContext context )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            next.render( context );
            uow.complete();
        }
        catch( Throwable e )
        {
            uow.discard();
        }
    }
}
