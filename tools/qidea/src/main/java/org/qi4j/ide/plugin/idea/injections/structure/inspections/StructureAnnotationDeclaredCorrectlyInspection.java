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
package org.qi4j.ide.plugin.idea.injections.structure.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;
import org.qi4j.ide.plugin.idea.injections.common.inspections.AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;
import static org.qi4j.ide.plugin.idea.injections.structure.common.Qi4jStructureAnnotationConstants.QUALIFIED_NAME_STRUCTURE_ANNOTATION;
import static org.qi4j.ide.plugin.idea.injections.structure.common.Qi4jStructureAnnotationUtil.StructureAnnotationDeclarationValidationResult;
import static org.qi4j.ide.plugin.idea.injections.structure.common.Qi4jStructureAnnotationUtil.validateStructureAnnotationDeclaration;

/**
 * {@code StructureAnnotationUsedCorrectly} validates {@code @Structure} injection annotation declaration.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public class StructureAnnotationDeclaredCorrectlyInspection
    extends AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "injections.structure.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "StructureAnnotationDeclaredCorrectlyInspection";
    }

    @NotNull
    protected final String getRemoveAnnotationMessageFix()
    {
        return message( "injections.structure.annotation.declared.correctly.fix.remove.annotation" );
    }

    @NotNull
    protected final String getAnnotationToCheckQualifiedName()
    {
        return QUALIFIED_NAME_STRUCTURE_ANNOTATION;
    }

    @Nullable
    protected final ProblemDescriptor[] verifyAnnotationDeclaredCorrectly( @NotNull PsiVariable psiVariable,
                                                                           @NotNull PsiAnnotation structureAnnotation,
                                                                           @NotNull InspectionManager manager )
    {
        StructureAnnotationDeclarationValidationResult annotationCheck =
            validateStructureAnnotationDeclaration( psiVariable );
        switch( annotationCheck )
        {
        case invalidInjectionType:
            String message = message(
                "injections.structure.annotation.declared.correctly.error.invalid.injection.type",
                psiVariable.getType().getCanonicalText()
            );
            AbstractFix removeStructureAnnotationFix = createRemoveAnnotationFix( structureAnnotation );
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                structureAnnotation, message, removeStructureAnnotationFix, GENERIC_ERROR_OR_WARNING
            );
            return new ProblemDescriptor[]{ problemDescriptor };
        }

        return null;
    }
}