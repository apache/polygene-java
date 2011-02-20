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
package org.qi4j.library.shiro.tests.x509;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.qi4j.library.shiro.domain.x509.X509Light;
import org.qi4j.library.shiro.realms.AbstractX509Qi4jRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrictX509Realm
        extends AbstractX509Qi4jRealm
{

    private static final Logger LOGGER = LoggerFactory.getLogger( StrictX509Realm.class );
    @Structure
    private QueryBuilderFactory qbf;

    public StrictX509Realm()
    {
        super();
        setName( StrictX509Realm.class.getSimpleName() );
        LOGGER.debug( "StringX509Realm instanciated" );
    }

    @Override
    protected Set<X509Certificate> getGrantedIssuers( X509Certificate userCertificate )
    {
        LOGGER.debug( "Loading Granted Issuers from x509 certificate: '{}'", userCertificate.getSubjectX500Principal() );
        Set<X509Certificate> grantedIssuers = new HashSet<X509Certificate>();
        grantedIssuers.add( X509FixturesData.usersCertificateAuthority() );
        return grantedIssuers;
    }

    @Override
    protected RoleAssignee getRoleAssignee( X509Certificate userCertificate )
    {
        LOGGER.debug( "Loading RoleAssignee from x509 certificate: '{}'", userCertificate.getSubjectX500Principal() );
        // FIXME This is done in two queries, could not get it to work in one :'(
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilder<X509Light> x509QueryBuilder = qbf.newQueryBuilder( X509Light.class );
        X509Light x509Template = templateFor( X509Light.class );
        x509QueryBuilder = x509QueryBuilder.where( and( eq( x509Template.canonicalIssuerDN(), userCertificate.getIssuerX500Principal().getName( X500Principal.CANONICAL ) ),
                                                        eq( x509Template.hexSerialNumber(), userCertificate.getSerialNumber().toString( 16 ) ) ) );
        Query<X509Light> x509Query = x509QueryBuilder.newQuery( uow ).maxResults( 1 );
        X509Light foundX509 = x509Query.iterator().next();
        if ( foundX509 == null ) {
            return null;
        }
        QueryBuilder<UserEntity> userQueryBuilder = qbf.newQueryBuilder( UserEntity.class );
        UserEntity userTemplate = templateFor( UserEntity.class );
        userQueryBuilder = userQueryBuilder.where( eq( userTemplate.x509().get().identity(), foundX509.identity().get() ) );
        Query<UserEntity> userQuery = userQueryBuilder.newQuery( uow ).maxResults( 1 );
        return userQuery.iterator().next();
    }

}
