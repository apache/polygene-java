/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.spaces.javaspaces;

import org.qi4j.library.spaces.tests.SpacesTestRig;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.jini.lookup.JiniLookupServiceAssembler;
import org.qi4j.library.jini.transaction.JiniTransactionServiceAssembler;
import org.qi4j.library.jini.javaspaces.JiniJavaSpacesServiceAssembler;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.injection.scope.Service;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.junit.Test;
import org.junit.Ignore;
import java.security.Policy;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.AllPermission;
import java.security.Permissions;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import net.jini.security.policy.DynamicPolicyProvider;

@Ignore( "Implementation is not working yet." )
public class JavaspacesTest extends SpacesTestRig
{
    private Space qi4jspace;

    static
    {
        Logger logger = Logger.getLogger( "" );
        Handler handler = null;
        try
        {
            handler = new FileHandler("output.log");
            Formatter formatter = new SimpleFormatter();
            handler.setFormatter( formatter );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        logger.addHandler( handler );
        logger.setLevel( Level.FINEST );
    }

    protected Space space()
    {
        if( qi4jspace == null )
        {
            qi4jspace = objectBuilderFactory.newObject( SpaceHolder.class ).getSpace();
        }
        return qi4jspace;
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( System.getSecurityManager() == null )
        {
            Policy allPolicy = new TestPolicyAllPermissions();
            Policy.setPolicy( new DynamicPolicyProvider(allPolicy) );
            System.setSecurityManager( new SecurityManager() );
        }
        module.addAssembler( new JiniLookupServiceAssembler() );
        module.addAssembler( new JiniTransactionServiceAssembler() );
        module.addAssembler( new JiniJavaSpacesServiceAssembler() );
        module.addAssembler( new JettyServiceAssembler() );
        module.addServices( MemoryEntityStoreService.class );
        module.addAssembler( new JavaSpacesClientAssembler() );
        module.addObjects( SpaceHolder.class );
    }

    @Test
    public void dummyTest()
    {
    }

    public static class SpaceHolder
    {
        @Service private Space space;

        public Space getSpace()
        {
            synchronized( this )
            {
                int count = 1000;
                while( !space.isReady() && count > 0 )
                {
                    try
                    {
                        wait(5);
                        count--;
                    }
                    catch( InterruptedException e )
                    {
                        e.printStackTrace();
                    }
                }
                if( !space.isReady() )
                {
                    throw new RuntimeException("Can not find the JavaSpace.");  
                }
            }
            return space;
        }
    }

    public static class TestPolicyAllPermissions extends Policy
    {
        public PermissionCollection getPermissions( CodeSource codeSource )
        {
            Permissions permissions;
            permissions = new Permissions();
            permissions.add( new AllPermission() );
            return permissions;
        }

        public void refresh()
        {
        }
    }
}
