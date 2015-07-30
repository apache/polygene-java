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
package org.apache.zest.ide.plugin.idea.concerns.intentions.add;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.zest.ide.plugin.idea.common.intentions.AbstractIntention;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.psi.search.searches.ClassInheritorsSearch.search;
import static java.util.Collections.emptyList;
import static org.apache.zest.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.apache.zest.ide.plugin.idea.concerns.common.Qi4jConcernUtil.addOrReplaceConcernAnnotation;
import static org.apache.zest.ide.plugin.idea.concerns.common.Qi4jConcernUtil.getConcernOfClass;

/**
 * JAVADOC: This is disabled in Qi4jApplicationComponent.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class AddConcernOnType
    extends AbstractIntention
{
    protected boolean isIntentionValidFor( PsiElement element )
    {
        if( !( element instanceof PsiClass ) )
        {
            return false;
        }

        // If it's not interface, ignore it
        PsiClass psiClass = (PsiClass) element;
        if( !psiClass.isInterface() )
        {
            return false;
        }

        // Is @Concerns accesible within module
        GlobalSearchScope searchScope = determineSearchScope( psiClass );
        PsiClass concernOfClass = getConcernOfClass( psiClass.getProject(), searchScope );
        return concernOfClass != null;
    }

    protected final String resourceBundlePrefixId()
    {
        return "add.concern";
    }

    @Override
    public boolean isAvailable( @NotNull Project project, Editor editor, @Nullable PsiElement element )
    {
        while( element != null )
        {
            if( element instanceof PsiFile ||
                element instanceof PsiMethod )
            {
                break;
            }

            if( isIntentionValidFor( element ) )
            {
                return true;
            }

            element = element.getParent();
        }

        return false;
    }

    @SuppressWarnings( "unchecked" )
    protected void processIntention( @NotNull Project project, @NotNull Editor editor, @NotNull PsiElement element )
    {
        PsiClass psiClass = (PsiClass) element;
        List<PsiClass> concernCandidates = findConcernsCandidates( psiClass );
        if( concernCandidates.size() == 1 )
        {
            PsiClass concernCandidate = concernCandidates.get( 0 );
            addOrReplaceConcernAnnotation( psiClass, concernCandidate );
        }
    }

    private static List<PsiClass> findConcernsCandidates( final @NotNull PsiClass classToCheck )
    {
        GlobalSearchScope searchScope = determineSearchScope( classToCheck );
        PsiClass concernOfClass = getConcernOfClass( classToCheck.getProject(), searchScope );
        if( concernOfClass == null )
        {
            return emptyList();
        }

        Query<PsiClass> psiClassQuery = search( concernOfClass, searchScope, true, false );
        final List<PsiClass> concernCandidates = new ArrayList<PsiClass>();
        psiClassQuery.forEach( new Processor<PsiClass>()
        {
            public boolean process( PsiClass psiClass )
            {
                // TODO: Ideally search for all "extends" as well
                boolean isInheritor = psiClass.isInheritor( classToCheck, true );
                if( isInheritor )
                {
                    concernCandidates.add( psiClass );
                }

                return true;
            }
        } );

        return concernCandidates;
    }
}

