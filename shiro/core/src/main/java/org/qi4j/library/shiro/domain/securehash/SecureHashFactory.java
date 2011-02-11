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
package org.qi4j.library.shiro.domain.securehash;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.CryptoException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
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
