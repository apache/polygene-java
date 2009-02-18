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
package org.qi4j.library.jini.importer.org.qi4j.library.jini.tests;

import java.security.Policy;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.AllPermission;
import java.security.Permissions;

public class Main
{
    private static InterpreterServiceImpl jiniService;

    public static void main( String[] args )
        throws Exception
    {
        Policy.setPolicy( new AllPolicy() );
        jiniService = new InterpreterServiceImpl( args );
        jiniService.startService();
        System.out.println( "Started 1!" );
        System.out.println(jiniService.getServiceID() );
        System.out.println( "Started 2!" );
    }

    private static class AllPolicy extends Policy
    {
        public PermissionCollection getPermissions( CodeSource codeSource )
        {
            final AllPermission allPermission = new AllPermission();
            final Permissions permissions = new Permissions();
            permissions.add( allPermission );
            return permissions;
        }

        public void refresh()
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
