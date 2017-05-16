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

package org.apache.polygene.ide.plugin.idea.mixins.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiAnnotationUtil.getAnnotationDefaultParameterValue;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiAnnotationUtil.getClassReference;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiClassUtil.getExtendsDeep;
import static org.apache.polygene.ide.plugin.idea.common.psi.PsiClassUtil.getPSIClass;
import static org.apache.polygene.ide.plugin.idea.concerns.common.PolygeneConcernUtil.isAConcern;
import static org.apache.polygene.ide.plugin.idea.mixins.common.PolygeneMixinConstants.QUALIFIED_NAME_MIXINS;
import static org.apache.polygene.ide.plugin.idea.sideEffects.common.PolygeneSideEffectUtil.isASideEffect;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PolygeneMixinUtil
{
    /**
     * Get all valid mixin types of given the {@code psiClass} argument.
     *
     * @param psiClass The psi class to check.
     * @return all vlaid mixin types of the given {@code psiClass} argument.
     * @since 0.1
     */
    @NotNull
    public static Set<PsiClass> getAllValidMixinTypes( @NotNull PsiClass psiClass )
    {
        PsiAnnotation mixinsAnnotation = getMixinsAnnotation( psiClass );
        if( mixinsAnnotation == null )
        {
            return emptySet();
        }

        Set<PsiClass> validMixinsType = getExtendsDeep( psiClass );
        validMixinsType.add( psiClass );
        return validMixinsType;
    }

    @NotNull
    public static List<PsiAnnotationMemberValue> getMixinsAnnotationValue( @NotNull PsiClass psiClass )
    {
        return getMixinsAnnotationValue( getMixinsAnnotation( psiClass ) );
    }

    @NotNull
    public static List<PsiAnnotationMemberValue> getMixinsAnnotationValue( @Nullable PsiAnnotation mixinsAnnotation )
    {
        if( mixinsAnnotation == null )
        {
            return emptyList();
        }

        String mixinsQualifiedName = mixinsAnnotation.getQualifiedName();
        if( !QUALIFIED_NAME_MIXINS.equals( mixinsQualifiedName ) )
        {
            return emptyList();
        }

        return getAnnotationDefaultParameterValue( mixinsAnnotation );
    }

    @Nullable
    public static PsiAnnotation getMixinsAnnotation( PsiElement element )
    {
        PsiClass psiClass = getPSIClass( element );
        if( psiClass == null )
        {
            return null;
        }

        return findAnnotation( psiClass, QUALIFIED_NAME_MIXINS );
    }

    @NotNull
    public static PsiAnnotation addOrReplaceMixinAnnotation( @NotNull PsiModifierListOwner modifierListOwner,
                                                             @NotNull PsiClass mixinClassToAdd )
    {
        Project project = modifierListOwner.getProject();
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        PsiElementFactory factory = psiFacade.getElementFactory();
        PsiAnnotation existingMixinsAnnotation = findAnnotation( modifierListOwner, QUALIFIED_NAME_MIXINS );

        boolean isReplace = false;
        PsiAnnotation newMixinsAnnotation;
        if( existingMixinsAnnotation != null )
        {
            // Check duplicate
            List<PsiAnnotationMemberValue> mixinsValues = getMixinsAnnotationValue( existingMixinsAnnotation );
            for( PsiAnnotationMemberValue mixinValue : mixinsValues )
            {
                PsiJavaCodeReferenceElement mixinClassReference = getMixinClassReference( mixinValue );
                if( mixinClassReference == null )
                {
                    continue;
                }

                PsiElement mixinClass = mixinClassReference.resolve();
                if( mixinClassToAdd.equals( mixinClass ) )
                {
                    return existingMixinsAnnotation;
                }
            }

            isReplace = true;
        }

        String mixinsAnnotationText = createMixinsAnnotationText( existingMixinsAnnotation, mixinClassToAdd );
        newMixinsAnnotation = factory.createAnnotationFromText( mixinsAnnotationText, modifierListOwner );

        if( isReplace )
        {
            // Replace @Mixins instead
            existingMixinsAnnotation.replace( newMixinsAnnotation );
        }
        else
        {
            // @Mixins doesn't exists, add it as first child
            PsiModifierList modifierList = modifierListOwner.getModifierList();
            modifierList.addBefore( newMixinsAnnotation, modifierList.getFirstChild() );
        }

        // Shorten all class references if possible
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance( project );
        codeStyleManager.shortenClassReferences( newMixinsAnnotation );

        return newMixinsAnnotation;
    }

    @NotNull
    private static String createMixinsAnnotationText( @Nullable PsiAnnotation mixinsAnnotationBase,
                                                      @NotNull PsiClass mixinClassToAdd )
    {
        StringBuilder annotationTextBuilder = new StringBuilder();
        annotationTextBuilder.append( "@" ).append( QUALIFIED_NAME_MIXINS ).append( "( {" );
        List<PsiAnnotationMemberValue> mixinsValues = getMixinsAnnotationValue( mixinsAnnotationBase );
        for( PsiAnnotationMemberValue mixinValue : mixinsValues )
        {
            annotationTextBuilder.append( mixinValue.getText() ).append( ", " );
        }
        annotationTextBuilder.append( mixinClassToAdd.getQualifiedName() ).append( ".class" );
        annotationTextBuilder.append( "} )" );

        return annotationTextBuilder.toString();
    }


    @Nullable
    public static PsiJavaCodeReferenceElement getMixinClassReference( @NotNull PsiAnnotationMemberValue value )
    {
        return getClassReference( value );
    }

    /**
     * Validate whether psiClass is a mixin.
     *
     * @param psiClass psi class to check.
     * @return {@code true} if psiClass is a mixin, {@code false} otherwise.
     */
    public static boolean isAMixin( @NotNull PsiClass psiClass )
    {
        return !( psiClass.isInterface() || isAConcern( psiClass ) || isASideEffect( psiClass ) );
    }

    private PolygeneMixinUtil()
    {
    }
}