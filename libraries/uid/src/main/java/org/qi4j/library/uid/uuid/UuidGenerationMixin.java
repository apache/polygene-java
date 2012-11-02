/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.uid.uuid;

import java.util.UUID;

public class UuidGenerationMixin
    implements UuidService
{
    private String uuid;
    private int count;

    public UuidGenerationMixin()
    {
        uuid = UUID.randomUUID().toString() + "-";
    }

    @Override
    public String generateUuid( int hashLength )
    {
        synchronized( this )
        {
            String uid = this.uuid + Integer.toHexString( count++ );
            if( hashLength > 0 )
            {
                byte[] result = new byte[hashLength];
                byte[] bytes = uid.getBytes();
                for( int i = 0; i < bytes.length; i++ )
                {
                    int pos = i % hashLength;
                    result[ pos ] = (byte) ( result[ pos ] * 31 + 19 + bytes[ i ] );
                }
                StringBuffer buf = new StringBuffer();
                for( int data : result )
                {
                    if( data < 0 )
                    {
                        data = 256 + data;
                    }
                    String hex = Integer.toHexString( data );
                    if( hex.length() == 1 )
                    {
                        hex = "0" + hex;
                    }
                    buf = buf.append( hex );
                }
                uid = buf.toString();
            }
            return uid.toUpperCase();
        }
    }
}
