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
