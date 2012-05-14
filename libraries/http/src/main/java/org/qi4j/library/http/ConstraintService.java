/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;

@Mixins( ConstraintService.Mixin.class )
public interface ConstraintService
        extends ServiceComposite
{

    ConstraintMapping buildConstraintMapping();

    static abstract class Mixin
            implements ConstraintService
    {

        @Service
        private ServiceReference<ConstraintService> myRef;

        public ConstraintMapping buildConstraintMapping()
        {
            ConstraintMapping csMapping = null;
            ConstraintInfo constraintInfo = myRef.metaInfo( ConstraintInfo.class );
            if ( constraintInfo != null && constraintInfo.getConstraint() != null ) {
                Constraint constraint = new Constraint();
                switch ( constraintInfo.getConstraint() ) {
                    case CLIENT_CERT:
                        constraint.setName( Constraint.__CERT_AUTH2 );
                        constraint.setAuthenticate( true );
                }
                csMapping = new ConstraintMapping();
                csMapping.setConstraint( constraint );
                csMapping.setPathSpec( constraintInfo.getPath() );
                if ( constraintInfo.getOmmitedHttpMethods() != null && constraintInfo.getOmmitedHttpMethods().length > 0 ) {
                    csMapping.setMethodOmissions( ConstraintInfo.HttpMethod.toStringArray( constraintInfo.getOmmitedHttpMethods() ) );
                }
            }
            return csMapping;
        }

        public void applyConstraints( Server server )
        {
        }

    }

}
