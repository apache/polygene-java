/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.ide.plugin.idea.appliesTo.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static java.util.Collections.emptyList;
import static org.qi4j.ide.plugin.idea.appliesTo.common.Qi4jAppliesToConstants.QUALIFIED_NAME_APPLIES_TO;
import static org.qi4j.ide.plugin.idea.appliesTo.common.Qi4jAppliesToConstants.QUALIFIED_NAME_APPLIES_TO_FILTER;
import static org.qi4j.ide.plugin.idea.common.psi.PsiAnnotationUtil.getAnnotationDefaultParameterValue;
import static org.qi4j.ide.plugin.idea.common.psi.PsiAnnotationUtil.getClassReference;
import static org.qi4j.ide.plugin.idea.common.psi.PsiClassUtil.getPSIClass;
import static org.qi4j.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jAppliesToUtil
{
    /**
     * @param searchContext Search context.
     * @return {@code AppliesToFilter} class given the search context. {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getAppliesToFilterClass( @NotNull PsiElement searchContext )
    {
        Project project = searchContext.getProject();
        GlobalSearchScope searchScope = determineSearchScope( searchContext );
        return getAppliesToFilterClass( project, searchScope );
    }

    /**
     * @param project project.
     * @param scope   search scope.
     * @return {@code AppliesToFilter} class given {@code project} and {@code scope} parameters.
     *         Returns {@code null} if not found.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getAppliesToFilterClass( @NotNull Project project,
                                                    @Nullable GlobalSearchScope scope )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        return scope == null ? null : psiFacade.findClass( QUALIFIED_NAME_APPLIES_TO_FILTER, scope );
    }

    /**
     * @param elementWithinJavaClass element within java class.
     * @return {@code @AppliesTo} annotation declaration of the class that contains the element.
     *         Returns {@code null} if not found, or {@code element} is an invalid context.
     * @since 0.1
     */
    @Nullable
    public static PsiAnnotation getAppliesToAnnotation( @NotNull PsiElement elementWithinJavaClass )
    {
        PsiClass psiClass = getPSIClass( elementWithinJavaClass );
        return findAnnotation( psiClass, QUALIFIED_NAME_APPLIES_TO );
    }

    /**
     * @param annotation annotation to process.
     * @return {@code @AppliesTo} annotation value. Returns {@link Collections#emptyList()} if {@code annotation} is
     *         {@code null} or annotation is not a {@code @AppliesTo} annotation.
     * @since 0.1
     */
    @NotNull
    public static List<PsiAnnotationMemberValue> getAppliesToAnnotationValue( @Nullable PsiAnnotation annotation )
    {
        if( annotation == null )
        {
            return emptyList();
        }

        String concernsQualifiedName = annotation.getQualifiedName();
        if( !QUALIFIED_NAME_APPLIES_TO.equals( concernsQualifiedName ) )
        {
            return emptyList();
        }

        return getAnnotationDefaultParameterValue( annotation );
    }

    /**
     * @param value annotation member value.
     * @return Applies to class reference given the {@code value} parameter. Returns {@code null} if it's not a
     *         class reference.
     * @since 0.1
     */
    @Nullable
    public static PsiJavaCodeReferenceElement getAppliesToValueClassReference( @NotNull PsiAnnotationMemberValue value )
    {
        return getClassReference( value );
    }

    /**
     * Returns a {@code boolean} indicator whether the specified {@code psiClass} is implements
     * {@code AppliesToFilter} class.
     *
     * @param psiClass             class to check.
     * @param appliesToFilterClass {@code AppliesToFilter} class.
     * @return {@code true} if {@code psiClass} implements {@code AppliesToFilter} class, {@code false} otherwise.
     * @since 0.1
     */
    public static boolean isAnAppliesToFilter( @NotNull PsiClass psiClass, @NotNull PsiClass appliesToFilterClass )
    {
        return !psiClass.isInterface() && psiClass.isInheritor( appliesToFilterClass, true );
    }

    private Qi4jAppliesToUtil()
    {
    }
}