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
package org.apache.zest.ide.plugin.idea.injections.structure.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static com.intellij.psi.PsiModifier.STATIC;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.ZestStructureAnnotationConstants.QUALIFIED_NAME_STRUCTURE_ANNOTATION;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.ZestStructureAnnotationConstants.VALID_STRUCTURE_INJECTION_TYPE;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.ZestStructureAnnotationUtil.StructureAnnotationDeclarationValidationResult.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class ZestStructureAnnotationUtil
{
    /**
     * Returns {@code Structure} annotation if exists.
     *
     * @param modifierListOwner Modifier list owner.
     * @return @Structure annotation if exists, {@code null} otherwise.
     * @since 0.1
     */
    @Nullable
    public static PsiAnnotation getStructureAnnotation( @NotNull PsiModifierListOwner modifierListOwner )
    {
        return findAnnotation( modifierListOwner, QUALIFIED_NAME_STRUCTURE_ANNOTATION );
    }

    /**
     * Create structure annotation.
     *
     * @param project project to create structure annotation.
     * @param context the context to create structure annotation.
     * @return @Structure annotation.
     */
    @NotNull
    public static PsiAnnotation createStructureAnnotation( @NotNull Project project,
                                                           @NotNull PsiElement context )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        PsiElementFactory factory = psiFacade.getElementFactory();
        return factory.createAnnotationFromText( "@" + QUALIFIED_NAME_STRUCTURE_ANNOTATION, context );
    }

    /**
     * @param variable variable to check.
     * @return Look at {@link StructureAnnotationDeclarationValidationResult}.
     * @since 0.1
     */
    @NotNull
    public static StructureAnnotationDeclarationValidationResult validateStructureAnnotationDeclaration(
        @NotNull PsiVariable variable )
    {
        PsiAnnotation structureAnnotation = getStructureAnnotation( variable );
        if( structureAnnotation == null )
        {
            return invalidStructureAnnotationNotDeclared;
        }

        PsiModifierList modifierList = variable.getModifierList();
        if( modifierList != null )
        {
            if( modifierList.hasModifierProperty( STATIC ) )
            {
                return invalidDeclaredOnStaticVariable;
            }
        }

        if( !isInjecteableByStructureAnnotation( variable ) )
        {
            return invalidInjectionType;
        }

        return valid;
    }

    /**
     * Returns a {@code boolean} indicator whether variable type is injectable by @Structure annotation.
     *
     * @param variable variable to check.
     * @return {@code true} if variable type is injecteable by @Structure annotation.
     * @since 0.1
     */
    public static boolean isInjecteableByStructureAnnotation( @NotNull PsiVariable variable )
    {
        PsiType type = variable.getType();
        String fieldClassQualifiedName = type.getCanonicalText();
        return binarySearch( VALID_STRUCTURE_INJECTION_TYPE, fieldClassQualifiedName ) > -1;
    }

    private ZestStructureAnnotationUtil()
    {
    }

    public enum StructureAnnotationDeclarationValidationResult
    {
        invalidStructureAnnotationNotDeclared,
        invalidDeclaredOnStaticVariable,
        invalidInjectionType,
        valid,
    }
}
