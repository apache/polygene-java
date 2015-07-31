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
package org.qi4j.ide.plugin.idea.injections.common.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static java.util.Arrays.asList;

/**
 * {@code AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection} is a helper method to check whether
 * injection annotation are declared in either constructor or non static field.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public abstract class AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection
    extends AbstractInjectionAnnotationDeclarationOnFieldInspection
{
    @Override
    public final ProblemDescriptor[] checkMethod( @NotNull PsiMethod method,
                                                  @NotNull InspectionManager manager,
                                                  boolean isOnTheFly )
    {
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        if( method.isConstructor() )
        {
            List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
            for( PsiParameter parameter : parameters )
            {
                PsiAnnotation annotation = getAnnotationToCheck( parameter );
                if( annotation != null )
                {
                    ProblemDescriptor[] descriptors =
                        verifyAnnotationDeclaredCorrectly( parameter, annotation, manager );
                    if( descriptors != null )
                    {
                        problems.addAll( asList( descriptors ) );
                    }
                }
            }

            return problems.toArray( new ProblemDescriptor[problems.size()] );
        }
        else
        {
            List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
            for( PsiParameter parameter : parameters )
            {
                PsiAnnotation annotationToCheck = getAnnotationToCheck( parameter );
                if( annotationToCheck != null )
                {
                    String message = getInjectionAnnotationValidDeclarationMessage();
                    AbstractFix removeAnnotationFix = createRemoveAnnotationFix( annotationToCheck );
                    ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                        annotationToCheck, message, removeAnnotationFix, GENERIC_ERROR_OR_WARNING
                    );
                    problems.add( problemDescriptor );
                }
            }

            return problems.toArray( new ProblemDescriptor[problems.size()] );
        }
    }
}
