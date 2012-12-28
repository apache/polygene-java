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
package org.qi4j.ide.plugin.idea.concerns.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.codeInsight.AnnotationUtil.findAnnotation;
import static java.util.Collections.emptyList;
import static org.qi4j.ide.plugin.idea.common.psi.PsiAnnotationUtil.getAnnotationDefaultParameterValue;
import static org.qi4j.ide.plugin.idea.common.psi.PsiAnnotationUtil.getClassReference;
import static org.qi4j.ide.plugin.idea.common.psi.PsiClassUtil.getPSIClass;
import static org.qi4j.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.qi4j.ide.plugin.idea.concerns.common.Qi4jConcernConstants.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jConcernUtil
{


    /**
     * @param searchContext Search context.
     * @return {@code GenericConcern} psi class if found, {@code null} otherwise.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getGenericConcernClass( @NotNull PsiElement searchContext )
    {
        Project project = searchContext.getProject();
        GlobalSearchScope searchScope = determineSearchScope( searchContext );
        return getGenericConcernClass( project, searchScope );
    }

    /**
     * @param project project.
     * @param scope   search scope.
     * @return {@code GenericConcern} psi class if found, {@code null} otherwise.
     * @since 0.1
     */
    @Nullable
    public static PsiClass getGenericConcernClass( @NotNull Project project,
                                                   @Nullable GlobalSearchScope scope )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        return scope != null ? psiFacade.findClass( QUALIFIED_NAME_GENERIC_CONCERN, scope ) : null;
    }

    @Nullable
    public static PsiClass getConcernOfClass( @NotNull PsiElement searchContext )
    {
        Project project = searchContext.getProject();
        GlobalSearchScope searchScope = determineSearchScope( searchContext );
        return getConcernOfClass( project, searchScope );
    }

    @Nullable
    public static PsiClass getConcernOfClass( @NotNull Project project,
                                              @Nullable GlobalSearchScope scope )
    {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        return scope != null ? psiFacade.findClass( QUALIFIED_NAME_CONCERN_OF, scope ) : null;
    }

    @Nullable
    public static PsiAnnotation getConcernsAnnotation( @NotNull PsiElement element )
    {
        PsiClass psiClass = getPSIClass( element );
        return findAnnotation( psiClass, QUALIFIED_NAME_CONCERNS );
    }

    @NotNull
    public static PsiAnnotation addOrReplaceConcernAnnotation( @NotNull PsiModifierListOwner modifierListOwner,
                                                               @NotNull PsiClass concernClassToAdd )
    {
        Project project = modifierListOwner.getProject();
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( project );
        PsiElementFactory factory = psiFacade.getElementFactory();
        PsiAnnotation existingConcernsAnnotation = findAnnotation( modifierListOwner, QUALIFIED_NAME_CONCERNS );

        boolean isReplace = false;
        PsiAnnotation newConcernsAnnotation;
        if( existingConcernsAnnotation != null )
        {
            // Check duplicate
            List<PsiAnnotationMemberValue> concernsValues = getConcernsAnnotationValue( existingConcernsAnnotation );
            for( PsiAnnotationMemberValue concernValue : concernsValues )
            {
                PsiJavaCodeReferenceElement concernClassReference = getConcernClassReference( concernValue );
                if( concernClassReference == null )
                {
                    continue;
                }

                PsiElement concernClass = concernClassReference.resolve();
                if( concernClassToAdd.equals( concernClass ) )
                {
                    return existingConcernsAnnotation;
                }
            }

            isReplace = true;
        }

        String concernAnnotationText = createConcernAnnotationText( existingConcernsAnnotation, concernClassToAdd );
        newConcernsAnnotation =
            factory.createAnnotationFromText( concernAnnotationText, modifierListOwner );

        if( isReplace )
        {
            // Replace @Concerns instead
            existingConcernsAnnotation.replace( newConcernsAnnotation );
        }
        else
        {
            // @Concerns doesn't exists, add it as first child
            PsiModifierList modifierList = modifierListOwner.getModifierList();
            modifierList.addBefore( newConcernsAnnotation, modifierList.getFirstChild() );
        }

        // Shorten all class references if possible
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance( project );
        codeStyleManager.shortenClassReferences( newConcernsAnnotation );

        return newConcernsAnnotation;
    }

    @NotNull
    private static String createConcernAnnotationText( @Nullable PsiAnnotation concernAnnotationBase,
                                                       @NotNull PsiClass concernClassToAdd )
    {
        StringBuilder annotationTextBuilder = new StringBuilder();
        annotationTextBuilder.append( "@" ).append( QUALIFIED_NAME_CONCERNS ).append( "( {" );
        List<PsiAnnotationMemberValue> concernsAnnotationValue = getConcernsAnnotationValue( concernAnnotationBase );
        for( PsiAnnotationMemberValue concernValue : concernsAnnotationValue )
        {
            annotationTextBuilder.append( concernValue.getText() ).append( ", " );
        }
        annotationTextBuilder.append( concernClassToAdd.getQualifiedName() ).append( ".class" );
        annotationTextBuilder.append( "} )" );
        return annotationTextBuilder.toString();
    }

    @NotNull
    public static List<PsiAnnotationMemberValue> getConcernsAnnotationValue( @Nullable PsiAnnotation concernsAnnotation )
    {
        if( concernsAnnotation == null )
        {
            return emptyList();
        }

        String concernsQualifiedName = concernsAnnotation.getQualifiedName();
        if( !QUALIFIED_NAME_CONCERNS.equals( concernsQualifiedName ) )
        {
            return emptyList();
        }

        return getAnnotationDefaultParameterValue( concernsAnnotation );
    }

    @Nullable
    public static PsiJavaCodeReferenceElement getConcernClassReference( @NotNull PsiAnnotationMemberValue value )
    {
        return getClassReference( value );
    }

    /**
     * @param psiClass psi class to check.
     * @return {@code true} if {@code psiClass} extends {@code ConcernOf}, {@code false} if {@code psiClass} does
     *         not extends {@code ConcernOf} or {@code ConcernOf} is not found.
     * @since 0.1
     */
    public static boolean isAConcern( @NotNull PsiClass psiClass )
    {
        if( psiClass.isInterface() )
        {
            return false;
        }

        PsiClass concernOfClass = getConcernOfClass( psiClass );
        return concernOfClass != null && psiClass.isInheritor( concernOfClass, true );
    }

    /**
     * @param psiClass psi class to check.
     * @return {@code true} if {@code psiClass} extends {@code GenericConcern}, {@code false} if {@code psiClass} does
     *         not extends {@code GenericConcern} or {@code GenericConcern} is not found.
     * @since 0.1
     */
    public static boolean isAGenericConcern( @NotNull PsiClass psiClass )
    {
        if( psiClass.isInterface() )
        {
            return false;
        }

        PsiClass genericConcern = getGenericConcernClass( psiClass );
        return genericConcern != null && psiClass.isInheritor( genericConcern, true );
    }

    private Qi4jConcernUtil()
    {
    }
}
