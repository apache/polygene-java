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
*/

package org.apache.polygene.ide.plugin.idea.injections.service.common;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static com.intellij.psi.PsiModifier.STATIC;
import static org.apache.polygene.ide.plugin.idea.injections.service.common.PolygeneServiceAnnotationConstants.QUALIFIED_NAME_SERVICE_ANNOTATION;
import static org.apache.polygene.ide.plugin.idea.injections.service.common.PolygeneServiceAnnotationUtil.ServiceAnnotationDeclarationValidationResult.*;
import static org.apache.polygene.ide.plugin.idea.injections.structure.common.PolygeneStructureAnnotationUtil.isInjecteableByStructureAnnotation;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneServiceAnnotationUtil
{
    /**
     * Returns {@code @Service} annotation if exists.
     *
     * @param modifierListOwner modifier list owner to process.
     * @return {@code @Service} annotation if exists, {@code null} otherwise.
     * @since 0.1
     */
    @Nullable
    public static PsiAnnotation getServiceAnnotation( @NotNull PsiModifierListOwner modifierListOwner )
    {
        return findAnnotation( modifierListOwner, QUALIFIED_NAME_SERVICE_ANNOTATION );
    }

    /**
     * Validates whether the variable has {@code @Service} annotation declared correctly.
     *
     * @param variable variable to check.
     * @return Look at {@link ServiceAnnotationDeclarationValidationResult}.
     * @since 0.1
     */
    @NotNull
    public static ServiceAnnotationDeclarationValidationResult isValidServiceAnnotationDeclaration(
        @NotNull PsiVariable variable )
    {
        PsiAnnotation serviceAnnotation = getServiceAnnotation( variable );
        if( serviceAnnotation == null )
        {
            return invalidServiceAnnotationNotDeclared;
        }

        PsiModifierList modifierList = variable.getModifierList();
        if( modifierList != null )
        {
            if( modifierList.hasModifierProperty( STATIC ) )
            {
                return invalidDeclaredOnStaticVariable;
            }
        }

        // Can't be type that is injected by @Structure
        if( isInjecteableByStructureAnnotation( variable ) )
        {
            return invalidTypeIsInjectedViaStructureAnnotation;
        }

        return valid;
    }

    public enum ServiceAnnotationDeclarationValidationResult
    {
        invalidServiceAnnotationNotDeclared,
        invalidDeclaredOnStaticVariable,
        invalidTypeIsInjectedViaStructureAnnotation,
        valid,
    }

    private PolygeneServiceAnnotationUtil()
    {
    }
}