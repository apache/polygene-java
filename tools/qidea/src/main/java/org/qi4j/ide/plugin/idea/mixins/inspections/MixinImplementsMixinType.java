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
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractFix;
import org.qi4j.ide.plugin.idea.common.inspections.AbstractInspection;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static org.qi4j.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernUtil.isAConcern;
import static org.qi4j.ide.plugin.idea.mixins.common.Qi4jMixinUtil.*;
import static org.qi4j.ide.plugin.idea.sideEffects.common.Qi4jSideEffectUtil.isASideEffect;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class MixinImplementsMixinType extends AbstractInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "mixin.implements.mixin.type";
    }

    @NotNull
    public final String getShortName()
    {
        return "MixinImplementsMixinType";
    }

    @Override
    public final ProblemDescriptor[] checkClass( @NotNull PsiClass psiClass,
                                                 @NotNull InspectionManager manager,
                                                 boolean isOnTheFly )
    {
        // If psiClass is not an interface, ignore
        if( !psiClass.isInterface() )
        {
            return null;
        }

        // If @Mixins annotation is empty, ignore
        List<PsiAnnotationMemberValue> mixinAnnotationValues = getMixinsAnnotationValue( psiClass );
        if( mixinAnnotationValues.isEmpty() )
        {
            return null;
        }

        // Get all valid mixin type
        Set<PsiClass> validMixinsType = getAllValidMixinTypes( psiClass );
        if( validMixinsType.isEmpty() )
        {
            return null;
        }

        // For each mixin
        List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
        for( PsiAnnotationMemberValue mixinAnnotationValue : mixinAnnotationValues )
        {
            PsiJavaCodeReferenceElement mixinClassReference = getMixinClassReference( mixinAnnotationValue );

            // If it's not a class reference, ignore
            if( mixinClassReference == null )
            {
                continue;
            }

            // If class reference can't be resolved, ignore
            PsiClass mixinClass = (PsiClass) mixinClassReference.resolve();
            if( mixinClass == null )
            {
                continue;
            }

            String mixinQualifiedName = mixinClass.getQualifiedName();

            boolean isMixinsDeclarationValid = false;
            String message = "";
            if( mixinClass.isInterface() )
            {
                // Mixin can't be an interface
                message = message( "mixin.implements.mixin.type.error.mixin.is.an.interface", mixinQualifiedName );
            }
            else if( isAConcern( mixinClass ) )
            {
                // Mixin can't be a concern
                message = message( "mixin.implements.mixin.type.error.mixin.is.a.concern", mixinQualifiedName );
            }
            else if( isASideEffect( mixinClass ) )
            {
                // Mixin can't be a side effect
                message = message( "mixin.implements.mixin.type.error.mixin.is.a.side.effect", mixinQualifiedName );
            }
            else
            {
                // If doesn't implement any mixin type, it's a problem
                if( !isImplementValidMixinType( mixinClass, validMixinsType ) )
                {
                    message = message(
                        "mixin.implements.mixin.type.error.does.not.implement.any.mixin.type",
                        mixinQualifiedName,
                        psiClass.getQualifiedName()
                    );
                }
                else
                {
                    isMixinsDeclarationValid = true;
                }
            }

            if( !isMixinsDeclarationValid )
            {
                ProblemDescriptor problemDescriptor = createProblemDescriptor(
                    manager, mixinAnnotationValue, mixinClassReference, message );
                problems.add( problemDescriptor );
            }
        }

        return problems.toArray( new ProblemDescriptor[problems.size()] );
    }

    private boolean isImplementValidMixinType( PsiClass mixinClass, Set<PsiClass> validMixinsType )
    {
        for( PsiClass validMixinTypeClass : validMixinsType )
        {
            if( mixinClass.isInheritor( validMixinTypeClass, true ) )
            {
                return true;
            }
        }

        return false;
    }

    private ProblemDescriptor createProblemDescriptor( @NotNull InspectionManager manager,
                                                       @NotNull PsiAnnotationMemberValue mixinAnnotationValue,
                                                       @NotNull PsiJavaCodeReferenceElement mixinClassReference,
                                                       @NotNull String message )
    {
        RemoveInvalidMixinClassReferenceFix fix = new RemoveInvalidMixinClassReferenceFix(
            mixinAnnotationValue, mixinClassReference
        );
        return manager.createProblemDescriptor( mixinAnnotationValue, message, fix, GENERIC_ERROR_OR_WARNING );
    }

    private static class RemoveInvalidMixinClassReferenceFix extends AbstractFix
    {
        private final PsiAnnotationMemberValue mixinClassAnnotationValue;

        public RemoveInvalidMixinClassReferenceFix( @NotNull PsiAnnotationMemberValue mixinClassAnnotationValue,
                                                    @NotNull PsiJavaCodeReferenceElement mixinClassReference )
        {
            super( message( "mixin.implements.mixin.type.fix.remove.class.reference", mixinClassReference.getQualifiedName() ) );
            this.mixinClassAnnotationValue = mixinClassAnnotationValue;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            mixinClassAnnotationValue.delete();
        }
    }
}
