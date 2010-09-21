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
package org.qi4j.library.shiro.authc;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.qi4j.library.shiro.crypto.HashFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecureHashCredentialsMatcher
        implements CredentialsMatcher
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SecureHashCredentialsMatcher.class );

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        UsernamePasswordToken usernamePasswordToken = ( UsernamePasswordToken ) token;
        SecureHashAuthenticationInfo secureHashAuthInfo = ( SecureHashAuthenticationInfo ) info;

        String tokenHash = HashFactory.create( secureHashAuthInfo.getSecureHash().hashAlgorithm().get(),
                                               usernamePasswordToken.getPassword(),
                                               secureHashAuthInfo.getSecureHash().salt().get(),
                                               secureHashAuthInfo.getSecureHash().hashIterations().get() ).toBase64();
        String authInfoHash = secureHashAuthInfo.getSecureHash().hash().get();

        LOGGER.debug( "tokenHash: {}", tokenHash );
        LOGGER.debug( "authInfoHash: {}", authInfoHash );

        return tokenHash.equals( authInfoHash );

    }

}
