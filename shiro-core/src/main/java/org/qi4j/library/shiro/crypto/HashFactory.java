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
package org.qi4j.library.shiro.crypto;

import java.util.Arrays;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.Md2Hash;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.Sha384Hash;
import org.apache.shiro.crypto.hash.Sha512Hash;

public class HashFactory
{

    private static final String[] VALID_ALGORITHM_NAMES = new String[]{ Md2Hash.ALGORITHM_NAME,
                                                                        Md5Hash.ALGORITHM_NAME,
                                                                        Sha1Hash.ALGORITHM_NAME,
                                                                        Sha256Hash.ALGORITHM_NAME,
                                                                        Sha384Hash.ALGORITHM_NAME,
                                                                        Sha512Hash.ALGORITHM_NAME };

    private HashFactory()
    {
    }

    public static Hash create( String algorithmName, Object source )
    {
        return create( algorithmName, source, null );
    }

    public static Hash create( String algorithmName, Object source, Object salt )
    {
        return create( algorithmName, source, salt, 1 );
    }

    public static Hash create( String algorithmName, Object source, Object salt, int hashIterations )
    {
        if ( algorithmName == null || algorithmName.length() <= 0 ) {
            throw new IllegalArgumentException( "Algorithm name was null or empty" );
        }
        if ( !Arrays.asList( VALID_ALGORITHM_NAMES ).contains( algorithmName ) ) {
            throw new IllegalArgumentException( algorithmName + " is not a valid algorithm. Valid ones are : " + Arrays.toString( VALID_ALGORITHM_NAMES ) );
        }
        if ( Md2Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Md2Hash( source, salt, hashIterations );
        }
        if ( Md5Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Md5Hash( source, salt, hashIterations );
        }
        if ( Sha1Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Sha1Hash( source, salt, hashIterations );
        }
        if ( Sha256Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Sha256Hash( source, salt, hashIterations );
        }
        if ( Sha384Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Sha384Hash( source, salt, hashIterations );
        }
        if ( Sha512Hash.ALGORITHM_NAME.equals( algorithmName ) ) {
            return new Sha512Hash( source, salt, hashIterations );
        }
        throw new InternalError( "You shall not pass!" );
    }

}
