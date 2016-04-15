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

package org.apache.zest.ide.plugin.idea.injections.service.inspections;

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
import static org.apache.zest.ide.plugin.idea.injections.service.common.ZestServiceAnnotationConstants.QUALIFIED_NAME_SERVICE_ANNOTATION;
import static org.apache.zest.ide.plugin.idea.injections.service.common.ZestServiceAnnotationUtil.ServiceAnnotationDeclarationValidationResult;
import static org.apache.zest.ide.plugin.idea.injections.service.common.ZestServiceAnnotationUtil.isValidServiceAnnotationDeclaration;
import static org.apache.zest.ide.plugin.idea.injections.structure.common.ZestStructureAnnotationUtil.getStructureAnnotation;

/**
 * {@code ServiceAnnotationDeclaredCorrectly} validates {@code @Service} injection annotation declaration.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public class ServiceAnnotationDeclaredCorrectlyInspection
    extends AbstractInjectionAnnotationDeclarationOnFieldAndConstructorInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "injections.service.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "ServiceAnnotationDeclaredCorrectlyInspection";
    }

    @NotNull
    protected final String getRemoveAnnotationMessageFix()
    {
        return message( "injections.service.annotation.declared.correctly.fix.remove.annotation" );
    }

    @NotNull
    protected final String getAnnotationToCheckQualifiedName()
    {
        return QUALIFIED_NAME_SERVICE_ANNOTATION;
    }

    @Nullable
    protected final ProblemDescriptor[] verifyAnnotationDeclaredCorrectly( @NotNull PsiVariable psiVariable,
                                                                           @NotNull PsiAnnotation serviceAnnotation,
                                                                           @NotNull InspectionManager manager )
    {
        ServiceAnnotationDeclarationValidationResult annotationCheck =
            isValidServiceAnnotationDeclaration( psiVariable );
        String message = null;
        LocalQuickFix fix = null;
        switch( annotationCheck )
        {
        case invalidTypeIsInjectedViaStructureAnnotation:
            if( getStructureAnnotation( psiVariable ) == null )
            {
                fix = new ReplaceWithStructureAnnotation(
                    message( "injections.service.annotation.declared.correctly.fix.replace.with.structure.annotation" ),
                    serviceAnnotation );
            }
            message = message(
                "injections.service.annotation.declared.correctly.error.type.is.injected.by.structure",
                psiVariable.getType().getCanonicalText()
            );
            break;
        }

        // If it's not an error, return null
        if( message == null )
        {
            return null;
        }

        // Default behavior to remove @Service annotation
        if( fix == null )
        {
            fix = createRemoveAnnotationFix( serviceAnnotation );
        }

        ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
            serviceAnnotation, message, fix, GENERIC_ERROR_OR_WARNING );
        return new ProblemDescriptor[]{ problemDescriptor };
    }
}