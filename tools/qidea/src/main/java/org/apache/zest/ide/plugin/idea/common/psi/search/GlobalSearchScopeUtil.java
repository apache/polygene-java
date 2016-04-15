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

package org.apache.zest.ide.plugin.idea.common.psi.search;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.module.ModuleUtil.findModuleForPsiElement;
import static org.apache.zest.ide.plugin.idea.common.vfs.VirtualFileUtil.getVirtualFile;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public class GlobalSearchScopeUtil
{
    /**
     * Determine search scope given a psi element.
     *
     * @param psiElement context.
     * @return Search scope given psi class.
     * @since 0.1
     */
    @Nullable
    public static GlobalSearchScope determineSearchScope( @NotNull PsiElement psiElement )
    {
        VirtualFile classVirtualFile = getVirtualFile( psiElement );
        if( classVirtualFile == null )
        {
            return null;
        }

        Module module = findModuleForPsiElement( psiElement );
        if( module == null )
        {
            return null;
        }

        Project project = psiElement.getProject();
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance( project );
        boolean includeTestClasses = projectRootManager.getFileIndex().isInTestSourceContent( classVirtualFile );
        return module.getModuleWithDependenciesAndLibrariesScope( includeTestClasses );
    }

}
