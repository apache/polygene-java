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
package org.qi4j.library.shiro.domain.securehash;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.shiro.crypto.CryptoException;
import org.qi4j.library.shiro.crypto.HashFactory;

@Mixins( SecureHashFactory.Mixin.class )
public interface SecureHashFactory
        extends ServiceComposite
{

    SecureHash create( char[] secret );

    abstract class Mixin
            implements SecureHashFactory
    {

        private static final String DEFAULT_ALGORITHM = Sha256Hash.ALGORITHM_NAME;
        private static final int DEFAULT_ITERATIONS = 1000;
        @Structure
        private ValueBuilderFactory vbf;

        public SecureHash create( char[] secret )
        {
            ValueBuilder<SecureHash> secureHashBuilder = vbf.newValueBuilder( SecureHash.class );
            SecureHash secureHash = secureHashBuilder.prototype();
            secureHash.hashAlgorithm().set( DEFAULT_ALGORITHM );
            secureHash.hashIterations().set( DEFAULT_ITERATIONS );
            String salt = generateSalt();
            String hash = HashFactory.create( DEFAULT_ALGORITHM, secret, salt, DEFAULT_ITERATIONS ).toBase64();
            secureHash.salt().set( salt );
            secureHash.hash().set( hash );
            return secureHashBuilder.newInstance();
        }

        private String generateSalt()
        {
            try {
                Long salt = new SecureRandom().nextLong();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream( bos );
                dos.writeLong( salt );
                dos.flush();
                return Base64.encodeToString( bos.toByteArray() );
            } catch ( IOException ex ) {
                throw new CryptoException( ex.getMessage(), ex );
            }
        }

    }

}
