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
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.openssl.PEMReader;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.shiro.crypto.CryptoException;

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
