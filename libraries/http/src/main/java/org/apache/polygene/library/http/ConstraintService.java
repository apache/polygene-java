/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.http;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.util.security.Constraint;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.library.http.ConstraintInfo.HttpMethod;

@Mixins( ConstraintService.Mixin.class )
public interface ConstraintService
{

    ConstraintMapping buildConstraintMapping();

    abstract class Mixin
            implements ConstraintService
    {

        @Service
        private ServiceReference<ConstraintService> myRef;

        @Override
        public ConstraintMapping buildConstraintMapping()
        {
            ConstraintMapping csMapping = null;
            ConstraintInfo constraintInfo = myRef.metaInfo( ConstraintInfo.class );
            if ( constraintInfo != null && constraintInfo.getConstraint() != null ) {
                Constraint constraint = new Constraint();
                csMapping = new ConstraintMapping();
                csMapping.setConstraint( constraint );
                csMapping.setPathSpec( constraintInfo.getPath() );
                if ( constraintInfo.getOmittedHttpMethods() != null && constraintInfo.getOmittedHttpMethods().length > 0 ) {
                    csMapping.setMethodOmissions( HttpMethod.toStringArray( constraintInfo.getOmittedHttpMethods() ) );
                }
            }
            return csMapping;
        }

    }

}
