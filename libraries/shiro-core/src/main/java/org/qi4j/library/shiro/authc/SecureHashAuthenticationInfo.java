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

import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.qi4j.library.shiro.domain.securehash.SecureHash;

public final class SecureHashAuthenticationInfo
        extends SimpleAuthenticationInfo
{

    private static final long serialVersionUID = 1L;
    private final String username;
    private final SecureHash secureHash;

    public SecureHashAuthenticationInfo( String username, SecureHash secureHash, String realmName )
    {
        super( username, secureHash, realmName );
        this.username = username;
        this.secureHash = secureHash;
    }

    public String getUsername()
    {
        return username;
    }

    public SecureHash getSecureHash()
    {
        return secureHash;
    }

}
