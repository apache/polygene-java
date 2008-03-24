/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entity;

import java.util.UUID;
import java.rmi.ServerError;
import java.rmi.dgc.VMID;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.UID;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;

public final class UuidIdentityGeneratorMixin
    implements IdentityGenerator
{
    protected String uuid;
    private int count;

    public UuidIdentityGeneratorMixin()
    {
        uuid = UUID.randomUUID().toString() + "-";
    }

    public String generate( Class<? extends Identity> compositeType )
    {
        return uuid + Integer.toHexString( count++ );
    }
}
