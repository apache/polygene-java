/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro.tests.username;

import org.qi4j.library.shiro.tests.SecuredService;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.library.shiro.ShiroAssembler;
import org.qi4j.library.shiro.domain.permissions.PermissionsDomainAssembler;
import org.qi4j.library.shiro.domain.securehash.SecureHashDomainAssembler;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class UsernameTestAssembler
        implements Assembler
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // Domain & Custom Realm
        module.addEntities( UserEntity.class );
        module.addServices( SecuredService.class );
        module.addObjects( UsernamePasswordRealm.class ); // Indirectly implements RealmActivator, used by the ShiroLifecycleService

        // Shiro Domain & Lifecycle
        new PermissionsDomainAssembler().assemble( module );
        new SecureHashDomainAssembler().assemble( module );
        new ShiroAssembler().assemble( module );

        // EntityStore & co
        module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
        new RdfMemoryStoreAssembler( null, Visibility.module, Visibility.module ).assemble( module );

        // Test Fixtures
        module.addServices( UsernameFixtures.class ).instantiateOnStartup();
    }

}
