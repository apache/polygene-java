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
package org.qi4j.library.shiro;

import org.qi4j.library.shiro.tests.SecuredService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.shiro.tests.username.UsernameFixtures;
import org.qi4j.library.shiro.tests.username.UsernameTestAssembler;
import org.qi4j.test.AbstractQi4jTest;

/**
 * A unit test showing how to use Shiro in Qi4j Applications with Username & Password credentials.
 *
 * This unit test use the provided domain Composites and Fragments to use secure password hashing as recommended
 * by the PKCS#5 standard: SHA-256 algorithm, 1000 iterations, random 64bit integer salt.
 */
public class UsernamePasswordTest
        extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new UsernameTestAssembler().assemble( module );
    }

    @Test
    public void test()
    {
        ServiceReference<SecuredService> ref = serviceLocator.findService( SecuredService.class );
        SecuredService secured = ref.get();
        secured.doSomethingThatRequiresNothing();
        try {
            secured.doSomethingThatRequiresUser();
            fail( "We're not authenticated, must fail" );
        } catch ( UnauthenticatedException ex ) {
        }
        try {
            secured.doSomethingThatRequiresPermissions();
            fail( "We're not authenticated, must fail" );
        } catch ( UnauthorizedException ex ) {
        }
        try {
            secured.doSomethingThatRequiresRoles();
            fail( "We're not authenticated, must fail" );
        } catch ( UnauthorizedException ex ) {
        }
        SecurityUtils.getSubject().login( new UsernamePasswordToken( UsernameFixtures.USERNAME, UsernameFixtures.PASSWORD ) );
        secured.doSomethingThatRequiresUser();
        secured.doSomethingThatRequiresPermissions();
        secured.doSomethingThatRequiresRoles();
    }

}
