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
package org.qi4j.ide.plugin.idea.mixins.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractInspection;
import static org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;
import static org.qi4j.ide.plugin.idea.mixins.common.Qi4jMixinUtil.getMixinsAnnotation;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class MixinsAnnotationDeclaredOnMixinType extends AbstractInspection
{
    @NotNull
    public final String getShortName()
    {
        return "MixinsAnnotationDeclaredOnMixinType";
    }

    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "mixins.annotation.declared.on.mixin.type";
    }

    @Override
    public ProblemDescriptor[] checkClass( @NotNull PsiClass psiClass,
                                           @NotNull InspectionManager manager,
                                           boolean isOnTheFly )
    {
        PsiAnnotation mixinsAnnotation = getMixinsAnnotation( psiClass );
        if( mixinsAnnotation == null )
        {
            return null;
        }

        if( psiClass.isInterface() )
        {
            return null;
        }

        String message = message( "mixins.annotation.declared.on.mixin.type.error.declared.on.class" );
        RemoveInvalidMixinClassReferenceFix fix = new RemoveInvalidMixinClassReferenceFix( mixinsAnnotation );
        ProblemDescriptor problemDescriptor = manager.createProblemDescriptor( mixinsAnnotation, message, fix,
                                                                               GENERIC_ERROR_OR_WARNING );
        return new ProblemDescriptor[]{ problemDescriptor };

    }

    private static class RemoveInvalidMixinClassReferenceFix extends AbstractFix
    {
        private final PsiAnnotationMemberValue mixinsAnnotation;

        public RemoveInvalidMixinClassReferenceFix( @NotNull PsiAnnotationMemberValue mixinsAnnotation )
        {
            super( message( "mixins.annotation.declared.on.mixin.type.fix.remove.mixins.annotation" ) );
            this.mixinsAnnotation = mixinsAnnotation;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            mixinsAnnotation.delete();
        }
    }
}
