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
package org.qi4j.library.shiro.domain.x509;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.joda.time.DateTime;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.shiro.crypto.CryptoException;

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
