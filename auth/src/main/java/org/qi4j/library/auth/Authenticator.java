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

package org.qi4j.library.auth;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWorkPropagation;

@Concerns( UnitOfWorkConcern.class )
@Mixins( Authenticator.DefaultAuthenticator.class )
public interface Authenticator
{
    UserPrincipal ANONYMOUS_USER = new UserPrincipal( null );

    @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
    UserPrincipal authenticate( String userId, String password );

    public class DefaultAuthenticator
        implements Authenticator
    {
        @Structure
        private UnitOfWorkFactory uowf;

        public UserPrincipal authenticate( String userId, String password )
        {
            if( password == null )
            {
                return ANONYMOUS_USER;
            }
            UnitOfWork uow = uowf.currentUnitOfWork();
            User user = uow.get( User.class, userId );
            if( !password.equals( user.hashedSecret().get() ) )
            {
                // Wrong password, do nothing!!
                return ANONYMOUS_USER;
            }
            return new UserPrincipal( user );
        }
    }
}
