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
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.apache.shiro.crypto.CryptoException;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.joda.time.DateTime;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( X509Factory.Mixin.class )
public interface X509Factory
        extends ServiceComposite
{

    X509 create( X509Certificate certificate );

    X509 create( Reader pemReader );

    abstract class Mixin
            implements X509Factory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        public X509 create( Reader pemReader )
        {
            try {
                return create( ( X509Certificate ) new PEMReader( pemReader ).readObject() );
            } catch ( ClassCastException ex ) {
                throw new IllegalArgumentException( "Given PEM Reader is not a valid X509Certificate PEM", ex );
            } catch ( IOException ex ) {
                throw new CryptoException( "Unable to read X509Certificate from PEM", ex );
            }
        }

        public X509 create( X509Certificate certificate )
        {
            try {
                UnitOfWork uow = uowf.currentUnitOfWork();
                EntityBuilder<X509> x509Builder = uow.newEntityBuilder( X509.class );
                X509 x509 = x509Builder.instance();
                x509.canonicalSubjectDN().set( certificate.getSubjectX500Principal().getName( X500Principal.CANONICAL ) );
                x509.hexSerialNumber().set( certificate.getSerialNumber().toString( 16 ) );
                x509.canonicalIssuerDN().set( certificate.getIssuerX500Principal().getName( X500Principal.CANONICAL ) );
                x509.issuanceDateTime().set( new DateTime( certificate.getNotBefore() ) );
                x509.expirationDateTime().set( new DateTime( certificate.getNotAfter() ) );
                StringWriter sw = new StringWriter();
                new PEMWriter( sw ).writeObject( certificate );
                x509.pem().set( sw.toString() );
                return x509Builder.newInstance();
            } catch ( IOException ex ) {
                throw new CryptoException( "Unable to write X509Certificate to PEM", ex );
            }
        }

    }

}
