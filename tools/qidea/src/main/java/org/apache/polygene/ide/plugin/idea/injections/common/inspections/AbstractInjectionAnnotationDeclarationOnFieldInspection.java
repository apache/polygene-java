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

package org.apache.polygene.ide.plugin.idea.injections.common.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiVariable;
import org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.polygene.ide.plugin.idea.common.inspections.AbstractFix;
import org.apache.polygene.ide.plugin.idea.common.inspections.AbstractInspection;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;

/**
 * @since 0.1
 */
public abstract class AbstractInjectionAnnotationDeclarationOnFieldInspection extends AbstractInspection
{
    /**
     * @return Remove annotation message fix.
     * @since 0.1
     */
    @NotNull
    protected abstract String getRemoveAnnotationMessageFix();

    /**
     * @return Annotation to check qualified name.
     * @since 0.1
     */
    @NotNull
    protected abstract String getAnnotationToCheckQualifiedName();

    /**
     * Verified that {@link #getAnnotationToCheck(com.intellij.psi.PsiVariable)} is declared correctly.
     *
     * @param psiVariable       Variable to check. This could be class field member or constructor parameter.
     * @param annotationToCheck annotation declared at variable to check.
     * @param manager           Inspection manager to use to create problem descriptor.
     * @return {@code null} if annotation is declared correctly, otherwise an array of problem descriptor.
     * @since 0.1
     */
    @Nullable
    protected abstract ProblemDescriptor[] verifyAnnotationDeclaredCorrectly( @NotNull PsiVariable psiVariable,
                                                                              @NotNull PsiAnnotation annotationToCheck,
                                                                              @NotNull InspectionManager manager );

    @Override
    public final ProblemDescriptor[] checkField( @NotNull PsiField field,
                                                 @NotNull InspectionManager manager,
                                                 boolean isOnTheFly )
    {
        PsiAnnotation annotationToCheck = getAnnotationToCheck( field );
        if( annotationToCheck == null )
        {
            return null;
        }

        PsiModifierList modifierList = field.getModifierList();
        if( modifierList != null )
        {
            if( modifierList.hasModifierProperty( com.intellij.psi.PsiModifier.STATIC ) )
            {
                String message = getInjectionAnnotationValidDeclarationMessage();
                AbstractFix removeAnnotationFix = createRemoveAnnotationFix( annotationToCheck );
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    annotationToCheck, message, removeAnnotationFix, com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );

                return new ProblemDescriptor[]{ problemDescriptor };
            }
        }

        return verifyAnnotationDeclaredCorrectly( field, annotationToCheck, manager );
    }

    /**
     * @param variable variable to check.
     * @return Annotation to check.
     * @see #getAnnotationToCheckQualifiedName()
     * @since 0.1
     */
    @Nullable
    protected final PsiAnnotation getAnnotationToCheck( @NotNull PsiVariable variable )
    {
        String annotationQualifiedName = getAnnotationToCheckQualifiedName();
        return findAnnotation( variable, annotationQualifiedName );
    }

    @NotNull protected String getInjectionAnnotationValidDeclarationMessage()
    {
        String annotationQualifiedName = getAnnotationToCheckQualifiedName();
        return PolygeneResourceBundle.message( "abstract.injection.annotation.declaration.inspection.error.annotation.not.declared.correctly",
                                           annotationQualifiedName );
    }

    @NotNull
    protected final AbstractFix createRemoveAnnotationFix( @NotNull PsiAnnotation annotationToRemove )
    {
        String fixMessage = getRemoveAnnotationMessageFix();
        return new RemoveAnnotationFix( fixMessage, annotationToRemove );
    }

    private static class RemoveAnnotationFix extends AbstractFix
    {
        private final PsiAnnotation annotationToRemove;

        public RemoveAnnotationFix( @NotNull String fixMessage, @NotNull PsiAnnotation annotationToRemove )
        {
            super( fixMessage );
            this.annotationToRemove = annotationToRemove;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            annotationToRemove.delete();
        }
    }
}
