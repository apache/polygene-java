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
package org.apache.zest.ide.plugin.idea.appliesTo.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.apache.zest.ide.plugin.idea.common.inspections.AbstractFix;
import org.apache.zest.ide.plugin.idea.common.inspections.AbstractInspection;

import java.util.LinkedList;
import java.util.List;

import static com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static org.apache.zest.ide.plugin.idea.appliesTo.common.ZestAppliesToUtil.*;
import static org.apache.zest.ide.plugin.idea.common.psi.PsiClassUtil.isImplementsInvocationHandler;
import static org.apache.zest.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.apache.zest.ide.plugin.idea.common.resource.ZestResourceBundle.message;
import static org.apache.zest.ide.plugin.idea.concerns.common.ZestConcernUtil.isAConcern;
import static org.apache.zest.ide.plugin.idea.concerns.common.ZestConcernUtil.isAGenericConcern;
import static org.apache.zest.ide.plugin.idea.sideEffects.common.ZestSideEffectUtil.isAGenericSideEffect;
import static org.apache.zest.ide.plugin.idea.sideEffects.common.ZestSideEffectUtil.isASideEffect;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class AppliesToAnnotationDeclaredCorrectlyInspection extends AbstractInspection
{
    @NotNull
    protected final String resourceBundlePrefixId()
    {
        return "applies.to.annotation.declared.correctly";
    }

    @NotNull
    public final String getShortName()
    {
        return "AppliesToAnnotationDeclaredCorrectlyInspection";
    }

    @Override
    public final ProblemDescriptor[] checkClass( @NotNull PsiClass psiClass,
                                                 @NotNull InspectionManager manager,
                                                 boolean isOnTheFly )
    {
        PsiAnnotation appliesToAnnotation = getAppliesToAnnotation( psiClass );
        if( appliesToAnnotation == null )
        {
            // If class does not have @AppliesTo, ignore
            return null;
        }

        String classQualifiedName = psiClass.getQualifiedName();
        // @AppliesTo can only be declared on class
        if( psiClass.isInterface() )
        {
            // Suggest remove applies to
            String message = message(
                "applies.to.annotation.declared.correctly.error.annotation.must.be.declared.on.class"
            );
            ProblemDescriptor problemDescriptor = createRemoveAppliesToFilterProblemDescriptor(
                manager, message, appliesToAnnotation );
            return new ProblemDescriptor[]{ problemDescriptor };
        }

        // If @AppliesTo annotation is empty, ignore
        List<PsiAnnotationMemberValue> appliesToAnnotationValues = getAppliesToAnnotationValue( appliesToAnnotation );
        if( appliesToAnnotationValues.isEmpty() )
        {
            return null;
        }

        // If AppliesToFilter is not resolved, ignore
        Project project = psiClass.getProject();
        GlobalSearchScope searchScope = determineSearchScope( psiClass );
        PsiClass appliesToFilterClass = getAppliesToFilterClass( project, searchScope );
        if( appliesToFilterClass == null )
        {
            return null;
        }

        boolean classIsAConcern = isAConcern( psiClass );
        boolean classIsASideEffect = isASideEffect( psiClass );
        boolean classIsAGenericConcern = classIsAConcern && isAGenericConcern( psiClass );
        boolean classIsAGenericSideEffect = classIsASideEffect && isAGenericSideEffect( psiClass );
        boolean classIsAMixin = !classIsAConcern && !classIsASideEffect;
        boolean classIsAGenericMixin = classIsAMixin && isImplementsInvocationHandler( psiClass );

        List<ProblemDescriptor> problems = new LinkedList<ProblemDescriptor>();
        for( PsiAnnotationMemberValue appliesToAnnotationValue : appliesToAnnotationValues )
        {
            PsiJavaCodeReferenceElement appliesToValueClassReference =
                getAppliesToValueClassReference( appliesToAnnotationValue );

            // If it's not a class reference, ignore
            if( appliesToValueClassReference == null )
            {
                continue;
            }

            // If class reference can't be resolved, ignore
            PsiClass appliesToValueClass = (PsiClass) appliesToValueClassReference.resolve();
            if( appliesToValueClass == null )
            {
                continue;
            }

            String appliesToValueQualifiedName = appliesToValueClass.getQualifiedName();
            boolean appliesToValueIsAnAnnotation = appliesToValueClass.isAnnotationType();
            boolean appliesToValueIsImplementingAppliesToFilter =
                appliesToValueClass.isInheritor( appliesToFilterClass, true );

            String message = null;
            if( appliesToValueIsAnAnnotation && classIsAMixin )
            {
                // If Class is a mixin and appliesToValueClass is an annotation
                message = message(
                    "applies.to.annotation.declared.correctly.error.value.is.invalid.for.mixin",
                    appliesToValueQualifiedName
                );
            }
            else if( appliesToValueIsAnAnnotation || appliesToValueIsImplementingAppliesToFilter )
            {
                if( classIsAConcern && !classIsAGenericConcern )
                {
                    // If psiClass is a concern but not generic concern
                    message = message(
                        "applies.to.annotation.declared.correctly.error.value.requires.class.to.extends.GenericConcern",
                        appliesToValueQualifiedName, classQualifiedName
                    );
                }
                else if( classIsASideEffect && !classIsAGenericSideEffect )
                {
                    // If psiClass a side effect but not a generic side effect
                    message = message(
                        "applies.to.annotation.declared.correctly.error.value.requires.class.to.extends.GenericSideEffect",
                        appliesToValueQualifiedName, classQualifiedName
                    );
                }
                else if( appliesToValueIsImplementingAppliesToFilter && !classIsAGenericMixin )
                {
                    message = message(
                        "applies.to.annotation.declared.correctly.error.value.requires.class.to.implements.InvocationHandler",
                        appliesToValueQualifiedName, classQualifiedName
                    );
                }
            }
            else if( appliesToValueClass.isInterface() )
            {
                if( !psiClass.isInheritor( appliesToValueClass, true ) &&
                    !( classIsAGenericConcern || classIsAGenericSideEffect ) )
                {
                    // If psiClass does not implement that interface and it's not a generic concern or generic side effect
                    if( classIsAConcern )
                    {
                        message = message(
                            "applies.to.annotation.declared.correctly.error.value.requires.class.to.implement.interface.or.extends.GenericConcern",
                            appliesToValueQualifiedName, classQualifiedName );
                    }
                    else if( classIsASideEffect )
                    {
                        message = message(
                            "applies.to.annotation.declared.correctly.error.value.requires.class.to.implement.interface.or.extends.GenericSideEffect",
                            appliesToValueQualifiedName, classQualifiedName );
                    }
                    else
                    {
                        message = message(
                            "applies.to.annotation.declared.correctly.error.value.requires.class.to.implement.value.interface.or.implements.InvocationHandler",
                            appliesToValueQualifiedName, classQualifiedName );
                    }
                }
            }
            else
            {
                if( classIsAMixin )
                {
                    message = message(
                        "applies.to.annotation.declared.correctly.error.value.is.invalid.for.mixin",
                        appliesToValueQualifiedName
                    );
                }
                else
                {
                    message = message(
                        "applies.to.annotation.declared.correctly.error.annotation.value.is.invalid.for.non.mixin",
                        appliesToValueQualifiedName
                    );
                }
            }

            if( message != null )
            {
                ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(
                    appliesToAnnotationValue,
                    message,
                    new RemoveAnnotationValueFix( appliesToAnnotationValue, appliesToValueClassReference ),
                    GENERIC_ERROR_OR_WARNING );
                problems.add( problemDescriptor );
            }
        }

        return problems.toArray( new ProblemDescriptor[problems.size()] );
    }

    @NotNull
    private ProblemDescriptor createRemoveAppliesToFilterProblemDescriptor( @NotNull InspectionManager manager,
                                                                            @NotNull String problemMessage,
                                                                            @NotNull PsiAnnotation appliesToAnnotation )
    {
        RemoveAppliesToFilterAnnotationFix fix = new RemoveAppliesToFilterAnnotationFix( appliesToAnnotation );
        return manager.createProblemDescriptor( appliesToAnnotation, problemMessage, fix, GENERIC_ERROR_OR_WARNING );
    }

    private static class RemoveAppliesToFilterAnnotationFix extends AbstractFix
    {
        private final PsiAnnotation appliesToFilterAnnotation;

        private RemoveAppliesToFilterAnnotationFix( @NotNull PsiAnnotation appliesToFilterAnnotation )
        {
            super( message( "applies.to.annotation.declared.correctly.fix.remove.annotation" ) );
            this.appliesToFilterAnnotation = appliesToFilterAnnotation;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            appliesToFilterAnnotation.delete();
        }
    }

    private static class RemoveAnnotationValueFix extends AbstractFix
    {
        private final PsiAnnotationMemberValue annotationValueToRemove;

        private RemoveAnnotationValueFix( @NotNull PsiAnnotationMemberValue annotationValueToRemove,
                                          @NotNull PsiJavaCodeReferenceElement appliesToValueClassReference )
        {
            super( message( "applies.to.annotation.declared.correctly.fix.remove.class.reference",
                            appliesToValueClassReference.getQualifiedName() ) );
            this.annotationValueToRemove = annotationValueToRemove;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            annotationValueToRemove.delete();
        }
    }

    private static class ClassImplementInterfaceFix extends AbstractFix
    {
        private final PsiClass psiClass;
        private final PsiClass interfaceToImplement;

        private ClassImplementInterfaceFix( @NotNull PsiClass psiClass,
                                            @NotNull PsiClass interfaceToImplement )
        {
            super( message( "applies.to.annotation.declared.correctly.fix.remove.class.reference",
                            interfaceToImplement.getQualifiedName() ) );
            this.psiClass = psiClass;
            this.interfaceToImplement = interfaceToImplement;
        }

        public final void applyFix( @NotNull Project project, @NotNull ProblemDescriptor descriptor )
        {
            PsiReferenceList implementList = psiClass.getImplementsList();
            if( implementList != null )
            {

                implementList.add( interfaceToImplement );
            }
        }
    }

}