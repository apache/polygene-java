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

package org.apache.zest.ide.plugin.idea.injections.invocation.common;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static com.intellij.psi.PsiModifier.STATIC;
import static org.apache.zest.ide.plugin.idea.common.psi.PsiClassUtil.getPSIClass;
import static org.apache.zest.ide.plugin.idea.injections.invocation.common.PolygeneInvocationAnnotationConstants.QUALIFIED_NAME_INVOCATION_ANNOTATION;
import static org.apache.zest.ide.plugin.idea.injections.invocation.common.PolygeneInvocationAnnotationUtil.InvocationAnnotationDeclarationValidationResult.*;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.PolygeneStructureAnnotationUtil.isInjecteableByStructureAnnotation;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneInvocationAnnotationUtil
{
    /**
     * Returns {@code @Invocation} annotation if exists.
     *
     * @param modifierListOwner modifier list owner to process.
     * @return {@code @Invocation} annotation if exists, {@code null} otherwise.
     * @since 0.1
     */
    @Nullable
    public static PsiAnnotation getInvocationAnnotation( @NotNull PsiModifierListOwner modifierListOwner )
    {
        return findAnnotation( modifierListOwner, QUALIFIED_NAME_INVOCATION_ANNOTATION );
    }

    /**
     * @param psiClass psi class to check.
     * @return {@code true} if the specified psiClass is injectable by invocation annotation, {@code false} otherwise.
     */
    public static boolean isInjectableByInvocationAnnotation( @NotNull PsiClass psiClass )
    {
        if( psiClass.isAnnotationType() )
        {
            return true;
        }

        String classQualifiedName = psiClass.getQualifiedName();
        return "java.lang.reflect.Method".equals( classQualifiedName ) ||
               "java.lang.reflect.AnnotatedElement".equals( classQualifiedName );
    }

    /**
     * Validates whether the variable has {@code @Invocation} annotation declared correctly.
     *
     * @param variable variable to check.
     * @return Look at {@link InvocationAnnotationDeclarationValidationResult}.
     * @since 0.1
     */
    @NotNull
    public static InvocationAnnotationDeclarationValidationResult isValidInvocationAnnotationDeclaration(
        @NotNull PsiVariable variable )
    {
        PsiAnnotation invocationAnnotation = getInvocationAnnotation( variable );
        if( invocationAnnotation == null )
        {
            return invalidInvocationAnnotationNotDeclared;
        }

        PsiModifierList modifierList = variable.getModifierList();
        if( modifierList != null )
        {
            if( modifierList.hasModifierProperty( STATIC ) )
            {
                return invalidDeclaredOnStaticVariable;
            }
        }

        // TODO: Check whether variable is either an instance of java.lang.reflect.Method or
        // java.lang.reflect.AnnotatedElement or Annotation
        PsiTypeElement typeElement = variable.getTypeElement();
        if( typeElement != null )
        {
            PsiClass psiClass = getPSIClass( typeElement );
            if( psiClass != null )
            {
                if( !isInjectableByInvocationAnnotation( psiClass ) )
                {
                    // Can't be type that is injected by @Structure
                    if( isInjecteableByStructureAnnotation( variable ) )
                    {
                        return invalidTypeIsInjectedViaStructureAnnotation;
                    }

                    return invalidType;
                }
            }
        }

        return valid;
    }

    public enum InvocationAnnotationDeclarationValidationResult
    {
        invalidInvocationAnnotationNotDeclared,
        invalidDeclaredOnStaticVariable,
        invalidTypeIsInjectedViaStructureAnnotation,
        invalidType,
        valid,
    }

    private PolygeneInvocationAnnotationUtil()
    {
    }
}