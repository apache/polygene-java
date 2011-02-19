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
package org.qi4j.ide.plugin.idea.concerns.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractInspection;
import static org.qi4j.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle;
import static org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernUtil.getConcernClassReference;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernUtil.getConcernOfClass;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernUtil.getConcernsAnnotation;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernUtil.getConcernsAnnotationValue;


/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class ConcernsAnnotationDeclaredCorrectlyInspection extends AbstractInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "concerns.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "ConcernsAnnotationDeclaredCorrectlyInspection";
    }

    @Override
    public final ProblemDescriptor[] checkClass( @NotNull PsiClass psiClass,
                                                 @NotNull InspectionManager manager,
                                                 boolean isOnTheFly )
    {
        // If class does not have @Concerns, ignore
        PsiAnnotation concernsAnnotation = getConcernsAnnotation( psiClass );
        if( concernsAnnotation == null )
        {
            return null;
        }

        // If @Concerns declared in class, suggest remove @Concerns annotation
        if( !psiClass.isInterface() )
        {
            String message = message( "concerns.annotation.declared.correctly.error.annotation.declared.in.class" );
            RemoveConcernsAnnotationFix fix = new RemoveConcernsAnnotationFix( concernsAnnotation );
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor( concernsAnnotation, message, fix,
                                                                                   GENERIC_ERROR_OR_WARNING );
            return new ProblemDescriptor[]{ problemDescriptor };
        }

        // If @Concerns annotation is empty, ignore
        List<PsiAnnotationMemberValue> concernsAnnotationValue = getConcernsAnnotationValue( concernsAnnotation );
        if( concernsAnnotationValue.isEmpty() )
        {
            return null;
        }

        // If ConcernOfClass is not resolved, ignore
        Project project = psiClass.getProject();
        GlobalSearchScope searchScope = determineSearchScope( psiClass );
        PsiClass concernOfClass = getConcernOfClass( project, searchScope );
        if( concernOfClass == null )
        {
            return null;
        }

        List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
        for( PsiAnnotationMemberValue concernClassAnnotationValue : concernsAnnotationValue )
        {
            PsiJavaCodeReferenceElement concernClassReference = getConcernClassReference( concernClassAnnotationValue );

            // If it's not a class reference, ignore
            if( concernClassReference == null )
            {
                continue;
            }

            // If class reference can't be resolved, ignore
            PsiClass concernClass = (PsiClass) concernClassReference.resolve();
            if( concernClass == null )
            {
                continue;
            }

            // If concern class does not inherit concern class, suggest remove that reference.
            if( !concernClass.isInheritor( concernOfClass, true ) )
            {
                String message = Qi4jResourceBundle.message(
                    "concerns.annotation.declared.correctly.error.concern.class.does.not.extend.ConcernOf",
                    concernClass.getQualifiedName()
                );

                RemoveInvalidConcernClassReferenceFix fix = new RemoveInvalidConcernClassReferenceFix(
                    concernClassAnnotationValue, concernClassReference
                );
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    concernClassAnnotationValue, message, fix, GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
            else
            {
                // TODO: Test whether it is a generic concern
                // TODO: Test whether it is a specific concern
            }
        }

        return problems.toArray( new ProblemDescriptor[problems.size()] );
    }

    private static class RemoveConcernsAnnotationFix extends AbstractFix
    {
        private final PsiAnnotation annotationToRemove;

        private RemoveConcernsAnnotationFix( @NotNull PsiAnnotation annotationToRemove )
        {
            super( message( "concerns.annotation.declared.correctly.fix.remove.annotation" ) );
            this.annotationToRemove = annotationToRemove;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            annotationToRemove.delete();
        }
    }

    private static class RemoveInvalidConcernClassReferenceFix extends AbstractFix
    {
        private final PsiAnnotationMemberValue concernClassAnnotationValue;

        public RemoveInvalidConcernClassReferenceFix( @NotNull PsiAnnotationMemberValue annotationValueToRemove,
                                                      @NotNull PsiJavaCodeReferenceElement concernClassReference )
        {
            super( message( "concerns.annotation.declared.correctly.fix.remove.concern.class.reference",
                            concernClassReference.getQualifiedName() ) );
            this.concernClassAnnotationValue = annotationValueToRemove;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            concernClassAnnotationValue.delete();
        }
    }
}
