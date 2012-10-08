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
package org.qi4j.library.shiro.domain.x509;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.apache.shiro.crypto.CryptoException;
import org.bouncycastle.openssl.PEMReader;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( X509LightFactory.Mixin.class )
public interface X509LightFactory
        extends ServiceComposite
{

    X509Light create( X500Principal subjectPrincipal, BigInteger serialNumber, X500Principal issuerPrincipal );

    X509Light create( String subjectPrincipal, String serialNumber, String issuerPrincipal );

    X509Light create( X509Certificate certificate );

    X509Light create( Reader pemReader );

    abstract class Mixin
            implements X509LightFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public X509Light create( X500Principal subjectPrincipal, BigInteger serialNumber, X500Principal issuerPrincipal )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<X509Light> x509LightBuilder = uow.newEntityBuilder( X509Light.class );
            X509Light x509Light = x509LightBuilder.instance();
            x509Light.canonicalSubjectDN().set( subjectPrincipal.getName( X500Principal.CANONICAL ) );
            x509Light.hexSerialNumber().set( serialNumber.toString( 16 ) );
            x509Light.canonicalIssuerDN().set( issuerPrincipal.getName( X500Principal.CANONICAL ) );
            return x509LightBuilder.newInstance();
        }

        public X509Light create( String subjectPrincipal, String hexSerialNumber, String issuerPrincipal )
        {
            X500Principal subjectX500Principal = new X500Principal( subjectPrincipal );
            BigInteger serNum = new BigInteger( hexSerialNumber, 16 );
            X500Principal issuerX500Principal = new X500Principal( issuerPrincipal );
            return create( subjectX500Principal, serNum, issuerX500Principal );
        }

        public X509Light create( X509Certificate certificate )
        {
            return create( certificate.getSubjectX500Principal(), certificate.getSerialNumber(), certificate.getIssuerX500Principal() );
        }

        public X509Light create( Reader pemReader )
        {
            try {
                return create( ( X509Certificate ) new PEMReader( pemReader ).readObject() );
            } catch ( ClassCastException ex ) {
                throw new IllegalArgumentException( "Given PEM Reader is not a valid X509Certificate PEM", ex );
            } catch ( IOException ex ) {
                throw new CryptoException( "Unable to read X509Certificate from PEM", ex );
            }
        }

    }

}
