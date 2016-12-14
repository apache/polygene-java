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

package org.apache.polygene.ide.plugin.idea.sideEffects.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static java.util.Collections.emptyList;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiAnnotationUtil.getAnnotationDefaultParameterValue;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiAnnotationUtil.getClassReference;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiClassUtil.getPSIClass;
import static org.apache.polygene.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.apache.polygene.ide.plugin.idea.sideEffects.common.PolygeneSideEffectConstants.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneSideEffectUtil
{
    /**
     * @param searchContext Search context.
     * @return {@code GenericSideEffect} class given the search context. {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getGenericSideEffectClass( @NotNull PsiElement searchContext )
    {
        Project project = searchContext.getProject();
        GlobalSearchScope searchScope = determineSearchScope( searchContext );
        return getGenericSideEffectClass( project, searchScope );
    }

    /**
     * @param project project.
     * @param scope   search scope.
     * @return {@code GenericSideEffect} class given {@code project} and {@code scope} parameters.
     *         Returns {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getGenericSideEffectClass( @NotNull Project project,
                                                      @Nullable GlobalSearchScope scope )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        return scope == null ? null : psiFacade.findClass( QUALIFIED_NAME_GENERIC_SIDE_EFFECT, scope );
    }

    /**
     * @param searchContext Search context.
     * @return {@code SideEffectOf} class given the search context. {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getSideEffectOfClass( @NotNull PsiElement searchContext )
    {
        Project project = searchContext.getProject();
        GlobalSearchScope searchScope = determineSearchScope( searchContext );
        return getSideEffectOfClass( project, searchScope );
    }


    /**
     * @param project project.
     * @param scope   search scope.
     * @return {@code SideEffectOf} class given {@code project} and {@code scope} parameters.
     *         Returns {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getSideEffectOfClass( @NotNull Project project,
                                                 @Nullable GlobalSearchScope scope )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        return scope == null ? null : psiFacade.findClass( QUALIFIED_NAME_SIDE_EFFECT_OF, scope );
    }

    /**
     * @param elementWithinJavaClass element within java class.
     * @return {@code @SideEffects} annotation declaration of the class that contains the element.
     *         Returns {@code null} if not found, or {@code element} is an invalid context.
     * @since 0.1
     */
    @Nullable
    public static PsiAnnotation getSideEffectsAnnotation( @NotNull PsiElement elementWithinJavaClass )
    {
        PsiClass psiClass = getPSIClass( elementWithinJavaClass );
        return findAnnotation( psiClass, QUALIFIED_NAME_SIDE_EFFECTS );
    }

    /**
     * @param annotation annotation to process.
     * @return {@code @SideEffects} annotation value. Returns {@link Collections#emptyList()} if {@code annotation} is
     *         {@code null} or annotation is not a {@code @SideEffects} annotation.
     * @since 0.1
     */
    @NotNull
    public static List<PsiAnnotationMemberValue> getSideEffectsAnnotationValue( @Nullable PsiAnnotation annotation )
    {
        if( annotation == null )
        {
            return emptyList();
        }

        String concernsQualifiedName = annotation.getQualifiedName();
        if( !QUALIFIED_NAME_SIDE_EFFECTS.equals( concernsQualifiedName ) )
        {
            return emptyList();
        }

        return getAnnotationDefaultParameterValue( annotation );
    }

    /**
     * @param value annotation member value.
     * @return Side effect class reference given the {@code value} parameter. Returns {@code null} if it's not a
     *         class reference.
     * @since 0.1
     */
    @Nullable
    public static PsiJavaCodeReferenceElement getSideEffectClassReference( @NotNull PsiAnnotationMemberValue value )
    {
        return getClassReference( value );
    }

    /**
     * Returns a {@code boolean} indicator whether the specified {@code psiClass} is a side effect.
     *
     * @param psiClass class to check.
     * @return {@code true} if {@code psiClass} is a side effect, {@code false} otherwise.
     * @since 0.1
     */
    public static boolean isASideEffect( @NotNull PsiClass psiClass )
    {
        if( psiClass.isInterface() )
        {
            return false;
        }

        PsiClass sideEffectOfClass = getSideEffectOfClass( psiClass );
        return sideEffectOfClass != null && psiClass.isInheritor( sideEffectOfClass, true );
    }

    /**
     * @param psiClass psi class to check.
     * @return {@code true} if {@code psiClass} inherits {@code GenericSideEffect} class, {@code false} if
     *         {@code psiClass} does
     *         not inherit {@code GenericSideEffect} or {@code GenericSideEffect} is not found.
     * @since 0.1
     */
    public static boolean isAGenericSideEffect( @NotNull PsiClass psiClass )
    {
        if( psiClass.isInterface() )
        {
            return false;
        }

        PsiClass genericSideEffect = getGenericSideEffectClass( psiClass );
        return genericSideEffect != null && psiClass.isInheritor( genericSideEffect, true );
    }

    private PolygeneSideEffectUtil()
    {
    }
}