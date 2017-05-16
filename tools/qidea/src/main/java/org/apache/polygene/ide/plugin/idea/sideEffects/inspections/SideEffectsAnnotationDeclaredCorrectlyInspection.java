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

package org.apache.polygene.ide.plugin.idea.sideEffects.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.apache.polygene.ide.plugin.idea.common.inspections.AbstractFix;
import org.apache.polygene.ide.plugin.idea.common.inspections.AbstractInspection;
import org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle;
import org.apache.polygene.ide.plugin.idea.sideEffects.common.PolygeneSideEffectUtil;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static org.apache.polygene.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle.message;
import static org.apache.polygene.ide.plugin.idea.sideEffects.common.PolygeneSideEffectUtil.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class SideEffectsAnnotationDeclaredCorrectlyInspection extends AbstractInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "side.effects.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "SideEffectsAnnotationDeclaredCorrectlyInspection";
    }

    @Override
    public final ProblemDescriptor[] checkClass( @NotNull PsiClass psiClass,
                                                 @NotNull InspectionManager manager,
                                                 boolean isOnTheFly )
    {
        // If class does not have @SideEffects, ignore
        PsiAnnotation sideEffectsAnnotation = getSideEffectsAnnotation( psiClass );
        if( sideEffectsAnnotation == null )
        {
            return null;
        }

        // If @SideEffects declared in class, suggest remove @SideEffects annotation
        if( !psiClass.isInterface() )
        {
            String message = message( "side.effects.annotation.declared.correctly.error.annotation.declared.in.class" );
            RemoveSideEffectsAnnotationFix fix = new RemoveSideEffectsAnnotationFix( sideEffectsAnnotation );
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor( sideEffectsAnnotation, message, fix,
                                                                                   GENERIC_ERROR_OR_WARNING );
            return new ProblemDescriptor[]{ problemDescriptor };
        }

        // If @SideEffects annotation is empty, ignore
        List<PsiAnnotationMemberValue> sideEffectsAnnotationValue =
            getSideEffectsAnnotationValue( sideEffectsAnnotation );
        if( sideEffectsAnnotationValue.isEmpty() )
        {
            return null;
        }

        // If SideEffectOf is not resolved, ignore
        Project project = psiClass.getProject();
        GlobalSearchScope searchScope = determineSearchScope( psiClass );
        PsiClass sideEffectOfClass = PolygeneSideEffectUtil.getGenericSideEffectClass( project, searchScope );
        if( sideEffectOfClass == null )
        {
            return null;
        }

        List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
        for( PsiAnnotationMemberValue sideEffectClassReferenceWrapper : sideEffectsAnnotationValue )
        {
            PsiJavaCodeReferenceElement sideEffectClassReference =
                getSideEffectClassReference( sideEffectClassReferenceWrapper );

            // If it's not a class reference, ignore
            if( sideEffectClassReference == null )
            {
                continue;
            }

            // If class reference can't be resolved, ignore
            PsiClass sideEffectClass = (PsiClass) sideEffectClassReference.resolve();
            if( sideEffectClass == null )
            {
                continue;
            }

            // If side effect class does not inherit SideEffectOf class, suggest remove that reference.
            if( !sideEffectClass.isInheritor( sideEffectOfClass, true ) )
            {
                String message = PolygeneResourceBundle.message(
                    "side.effects.annotation.declared.correctly.error.side.effect.does.not.extend.side.effect.of",
                    sideEffectClass.getQualifiedName()
                );

                RemoveAnnotationValueFix fix = new RemoveAnnotationValueFix(
                    sideEffectClassReferenceWrapper, sideEffectClassReference
                );
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    sideEffectClassReferenceWrapper, message, fix, GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
            else
            {
                // TODO: Test whether it is a generic side effect
                // TODO: Test whether it is a specific side effect
            }
        }

        return problems.toArray( new ProblemDescriptor[problems.size()] );
    }

    private static class RemoveSideEffectsAnnotationFix extends AbstractFix
    {
        private final PsiAnnotation sideEffectsAnnotation;

        private RemoveSideEffectsAnnotationFix( @NotNull PsiAnnotation sideEffectsAnnotation )
        {
            super( message( "side.effects.annotation.declared.correctly.fix.remove.annotation" ) );
            this.sideEffectsAnnotation = sideEffectsAnnotation;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            sideEffectsAnnotation.delete();
        }
    }

    private static class RemoveAnnotationValueFix extends AbstractFix
    {
        private final PsiAnnotationMemberValue annotationValueToRemove;

        private RemoveAnnotationValueFix( @NotNull PsiAnnotationMemberValue annotationValueToRemove,
                                          @NotNull PsiJavaCodeReferenceElement sideEffectClassReference )
        {
            super( message( "side.effects.annotation.declared.correctly.fix.remove.class.reference",
                            sideEffectClassReference.getQualifiedName() ) );
            this.annotationValueToRemove = annotationValueToRemove;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            annotationValueToRemove.delete();
        }
    }
}