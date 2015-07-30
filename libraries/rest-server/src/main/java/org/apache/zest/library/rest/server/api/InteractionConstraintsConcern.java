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
 */
package org.apache.zest.library.rest.server.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.AppliesToFilter;
import org.apache.zest.api.concern.GenericConcern;
import org.apache.zest.api.constraint.ConstraintDeclaration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.structure.Module;
import org.apache.zest.library.rest.server.api.constraint.InteractionConstraintDeclaration;
import org.apache.zest.library.rest.server.api.constraint.RequiresValid;
import org.apache.zest.library.rest.server.restlet.InteractionConstraints;

/**
 * Add this concern to all interaction methods that use constraints
 */
@AppliesTo( InteractionConstraintsConcern.HasInteractionConstraints.class )
public class InteractionConstraintsConcern
    extends GenericConcern
{
    @Service
    private InteractionConstraints interactionConstraints;

    @Service
    private Module module;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( !interactionConstraints.isValid( proxy.getClass(), ObjectSelection.current(), module ) )
        {
            throw new IllegalStateException( "Not allowed to invoke interaction " + method.getName() );
        }

        if( !interactionConstraints.isValid( method, ObjectSelection.current(), module ) )
        {
            throw new IllegalStateException( "Not allowed to invoke interaction " + method.getName() );
        }

        return next.invoke( proxy, method, args );
    }

    public static class HasInteractionConstraints
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            for( Annotation annotation : method.getAnnotations() )
            {
                if( annotation.annotationType().equals( RequiresValid.class ) ||
                    annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null ||
                    annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ) != null )
                {
                    return true;
                }
            }
            return false;
        }
    }
}
