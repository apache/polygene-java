/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.library.restlet.repository;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.apache.zest.api.constraint.ConstraintDeclaration;
import org.apache.zest.api.constraint.Constraints;
import org.apache.zest.library.restlet.identity.IdentityManager;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( EntityName.Constraint.class )
public @interface EntityName
{

    class Constraint
        implements org.apache.zest.api.constraint.Constraint<EntityName, String>
    {

        @Override
        public boolean isValid( EntityName annotation, String value )
        {
            int pos = value.indexOf( IdentityManager.SEPARATOR );
            if( pos > 0 )
            {
                value = value.substring( pos+1 );
            }
            for( int i = 0; i < value.length(); i++ )
            {
                char ch = value.charAt( i );
                if( ( ch < 'A' || ch > 'Z' ) &&
                    ( ch < 'a' || ch > 'z' ) &&
                    ( ch < '0' || ch > '9' ) &&
                    ( ch != '.' ) &&
                    ( ch != '_' ) &&
                    ( ch != '-' )
                    )
                {
                    return false;
                }
            }
            return true;
        }
    }
}
