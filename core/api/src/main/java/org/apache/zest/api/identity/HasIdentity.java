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
package org.apache.zest.api.identity;

import java.lang.reflect.Method;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;

/**
 * This interface provides the identity of the object which may be used
 * to store the state in a database. It is not the responsibility of the
 * framework to come up with a good identity string.
 */
@Mixins( HasIdentity.HasIdentityMixin.class )
public interface HasIdentity
{
    Method IDENTITY_METHOD = HasIdentityMixin.identityMethod();
    QualifiedName IDENTITY_STATE_NAME = HasIdentityMixin.stateName();

    @Immutable
    Property<Identity> identity();

    /**
     * Default Identity implementation.
     */
    class HasIdentityMixin
        implements HasIdentity
    {
        @State
        private Property<Identity> identity;


        @Override
        public Property<Identity> identity()
        {
            return identity;
        }

        private static QualifiedName stateName()
        {
            return QualifiedName.fromAccessor( identityMethod() );
        }

        private static Method identityMethod()
        {
            try
            {
                return HasIdentity.class.getMethod( "identity" );
            }
            catch( NoSuchMethodException e )
            {
                throw new InternalError( "Polygene Core Runtime codebase is corrupted." );
            }
        }
    }
}
