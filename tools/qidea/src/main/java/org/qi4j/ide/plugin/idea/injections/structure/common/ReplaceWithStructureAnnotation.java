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
package org.qi4j.ide.plugin.idea.injections.structure.common;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;
import static org.qi4j.ide.plugin.idea.injections.structure.common.Qi4jStructureAnnotationUtil.createStructureAnnotation;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class ReplaceWithStructureAnnotation extends AbstractFix
{
    private final PsiAnnotation annotation;

    public ReplaceWithStructureAnnotation( @NotNull String fixMessage,
                                           @NotNull PsiAnnotation annotationToReplace )
    {
        super( fixMessage );
        this.annotation = annotationToReplace;
    }

    public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
    {
        PsiAnnotation structureAnnotation = createStructureAnnotation( project, annotation );
        annotation.replace( structureAnnotation );
    }
}