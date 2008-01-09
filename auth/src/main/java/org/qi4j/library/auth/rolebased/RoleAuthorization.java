/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.library.auth.rolebased;

import org.qi4j.library.auth.AuthorizationService;
import org.qi4j.library.auth.AuthorizationManagement;
import org.qi4j.library.auth.Role;
import org.qi4j.library.auth.User;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.association.ManyAssociation;
import org.qi4j.association.MapAssociation;
//import org.qi4j.association.AssociationChangeObserver;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;                     

public class RoleAuthorization
    implements AuthorizationService, AuthorizationManagement
{
    private User currentUser;
    private MapAssociation<Method, ManyAssociation<Role>> methodToRoles;


    public RoleAuthorization()
    {
//        methodToRoles = new MapAssociation<Method, ManyAssociation<Role>>()
//        {
//            public ManyAssociation<Role> get( Method key )
//            {
//                //TODO: Auto-generated, need attention.
//                return null;
//            }
//
//            public void put( Method key, ManyAssociation<Role> value )
//            {
//                //TODO: Auto-generated, need attention.
//
//            }
//
//            public ManyAssociation<Role> remove( Method key )
//            {
//                //TODO: Auto-generated, need attention.
//                return null;
//            }
//
//            public void addChangeObserver( AssociationChangeObserver associationChangeObserver )
//            {
//                //TODO: Auto-generated, need attention.
//
//            }
//
//            public Iterator<Map.Entry<Method, ManyAssociation<Role>>> iterator()
//            {
//                //TODO: Auto-generated, need attention.
//                return null;
//            }
//        };
    }

    public void authorize( CompositeBinding compositeType, Method method, Object[] args )
        throws SecurityException
    {
        if( currentUser == null ) // TODO: If no User, should the call be allowed or not?
        {
            return;
        }
        
        ManyAssociation<Role> roles = methodToRoles.get( method );
        if( roles != null )
        {
            for( Role role : currentUser.roles() )
            {
                if( roles.contains( role ) )
                {
                    // authorized?
                    return;
                }
            }
        }
        throw new SecurityException( "Unauthorized Access: " + compositeType.getCompositeResolution().getCompositeModel().getCompositeClass().getName() + "." + method.getName() );
    }

    public void establishCurrentUser( User user )
    {
        currentUser = user;
    }
}
