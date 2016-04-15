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

package org.apache.zest.ide.plugin.idea.injections.invocation.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.zest.ide.plugin.idea.injections.common.inspections.AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection;
import org.apache.zest.ide.plugin.idea.injections.structure.common.ReplaceWithStructureAnnotation;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static org.apache.zest.ide.plugin.idea.common.resource.ZestResourceBundle.message;
import static org.apache.zest.ide.plugin.idea.injections.invocation.common.ZestInvocationAnnotationConstants.QUALIFIED_NAME_INVOCATION_ANNOTATION;
import static org.apache.zest.ide.plugin.idea.injections.invocation.common.ZestInvocationAnnotationUtil.InvocationAnnotationDeclarationValidationResult;
import static org.apache.zest.ide.plugin.idea.injections.invocation.common.ZestInvocationAnnotationUtil.isValidInvocationAnnotationDeclaration;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.ZestStructureAnnotationUtil.getStructureAnnotation;

/**
 * {@code InvocationAnnotationDeclaredCorrectlyInspection} validates {@code @Invocation} injection annotation
 * declaration.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public class InvocationAnnotationDeclaredCorrectlyInspection
    extends AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "injections.invocation.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "InvocationAnnotationDeclaredCorrectlyInspection";
    }

    @NotNull
    protected final String getRemoveAnnotationMessageFix()
    {
        return message( "injections.invocation.annotation.declared.correctly.fix.remove.annotation" );
    }

    @NotNull
    protected final String getAnnotationToCheckQualifiedName()
    {
        return QUALIFIED_NAME_INVOCATION_ANNOTATION;
    }

    @Nullable
    protected final ProblemDescriptor[] verifyAnnotationDeclaredCorrectly( @NotNull PsiVariable psiVariable,
                                                                           @NotNull PsiAnnotation invocationAnnotation,
                                                                           @NotNull InspectionManager manager )
    {
        LocalQuickFix fix = null;
        String message = null;

        String variableTypeQualifiedName = psiVariable.getType().getCanonicalText();

        InvocationAnnotationDeclarationValidationResult validationResult =
            isValidInvocationAnnotationDeclaration( psiVariable );
        switch( validationResult )
        {
        case invalidTypeIsInjectedViaStructureAnnotation:
            if( getStructureAnnotation( psiVariable ) == null )
            {
                fix = new ReplaceWithStructureAnnotation(
                    message( "injections.invocation.annotation.declared.correctly.fix.replace.with.structure.annotation" ),
                    invocationAnnotation );
            }
            message = message(
                "injections.invocation.annotation.declared.correctly.error.type.is.injected.by.structure",
                variableTypeQualifiedName
            );
            break;

        case invalidType:
            message = message( "injections.invocation.annotation.declared.correctly.error.type.is.not.injectable",
                               variableTypeQualifiedName );
            break;
        }

        // If it's not an error, return null
        if( message == null )
        {
            return null;
        }

        // If Fix not defined, by default we remove it.
        if( fix == null )
        {
            fix = createRemoveAnnotationFix( invocationAnnotation );
        }

        ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
            invocationAnnotation, message, fix, GENERIC_ERROR_OR_WARNING );
        return new ProblemDescriptor[]{ problemDescriptor };
    }
}