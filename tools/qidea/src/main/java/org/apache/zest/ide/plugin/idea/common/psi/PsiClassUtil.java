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
package org.apache.zest.ide.plugin.idea.common.psi;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.Set;

import static org.apache.zest.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class PsiClassUtil
{
    @Nullable
    public static PsiClass getPSIClass( @NotNull PsiElement element )
    {
        if( element instanceof PsiClass )
        {
            return (PsiClass) element;
        }

        if( element instanceof PsiTypeElement )
        {
            PsiTypeElement psiTypeElement = (PsiTypeElement) element;
            PsiJavaCodeReferenceElement componentRef = psiTypeElement.getInnermostComponentReferenceElement();
            if( componentRef == null )
            {
                return null;
            }

            return (PsiClass) componentRef.resolve();
        }

        PsiElement context = element.getContext();
        if( context instanceof PsiClass )
        {
            return (PsiClass) context;
        }

        return null;
    }

    @NotNull
    public static Set<PsiClass> getExtends( @NotNull PsiClass psiClass )
    {
        HashSet<PsiClass> extendsClasses = new HashSet<PsiClass>();
        PsiClassType[] extendsClassTypes = psiClass.getExtendsListTypes();
        for( PsiClassType extendClassType : extendsClassTypes )
        {
            PsiClass extendClass = extendClassType.resolve();
            if( extendClass != null )
            {
                extendsClasses.add( extendClass );
            }
        }

        return extendsClasses;
    }

    /**
     * Returns all extends of the specified {@code psiClass}.
     *
     * @param psiClass class to process.
     * @return all extends of the specified {@code psiClass}.
     * @since 0.1
     */
    @NotNull
    public static Set<PsiClass> getExtendsDeep( @NotNull PsiClass psiClass )
    {
        HashSet<PsiClass> extendsClasses = new HashSet<PsiClass>();
        PsiClassType[] extendsClassTypes = psiClass.getExtendsListTypes();
        for( PsiClassType extendClassType : extendsClassTypes )
        {
            PsiClass extendClass = extendClassType.resolve();
            if( extendClass != null )
            {
                extendsClasses.add( extendClass );
                extendsClasses.addAll( getExtendsDeep( extendClass ) );
            }
        }

        return extendsClasses;
    }

    /**
     * @param psiClass Psi class to check.
     * @return {@code true} if psi class implements {@code InvocationHandler}, {@code false} otherwise.
     * @see InvocationHandler
     */
    public static boolean isImplementsInvocationHandler( @NotNull PsiClass psiClass )
    {
        if( psiClass.isInterface() )
        {
            return false;
        }

        GlobalSearchScope searchScope = determineSearchScope( psiClass );
        assert searchScope != null;

        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance( psiClass.getProject() );
        PsiClass invocationHandler = psiFacade.findClass( "java.lang.reflect.InvocationHandler", searchScope );
        assert invocationHandler != null;

        return psiClass.isInheritor( invocationHandler, true );
    }

    private PsiClassUtil()
    {
    }
}
