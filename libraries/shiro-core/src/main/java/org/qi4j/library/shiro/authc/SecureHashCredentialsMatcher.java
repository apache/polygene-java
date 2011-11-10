/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.shiro.authc;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.qi4j.library.shiro.Shiro;
import org.qi4j.library.shiro.crypto.HashFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecureHashCredentialsMatcher
        implements CredentialsMatcher
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

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
