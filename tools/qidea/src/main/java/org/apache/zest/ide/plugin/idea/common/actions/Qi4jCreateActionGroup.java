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
package org.apache.zest.ide.plugin.idea.common.actions;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;

import static org.apache.zest.ide.plugin.idea.common.resource.Qi4jResourceBundle.message;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class Qi4jCreateActionGroup extends DefaultActionGroup
{
    public Qi4jCreateActionGroup()
    {
        super( message( "qi4j.action.group.title" ), true );
        getTemplatePresentation().setDescription( message( "qi4j.action.group.description" ) );
    }

    public void update( AnActionEvent e )
    {
        Presentation presentation = e.getPresentation();
        presentation.setVisible( shouldActionGroupVisible( e ) );
    }

    private boolean shouldActionGroupVisible( AnActionEvent e )
    {
        Module module = e.getData( LangDataKeys.MODULE );
        if( module == null )
        {
            return false;
        }

        // TODO: Enable this once Qi4jFacet can be automatically added/removed
//        if( Qi4jFacet.getInstance( module ) == null )
//        {
//            return false;
//        }

        // Are we on IDE View and under project source folder?
        Project project = e.getData( PlatformDataKeys.PROJECT );
        IdeView view = e.getData( LangDataKeys.IDE_VIEW );
        if( view != null && project != null )
        {
            ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance( project ).getFileIndex();
            PsiDirectory[] dirs = view.getDirectories();
            for( PsiDirectory dir : dirs )
            {
                if( projectFileIndex.isInSourceContent( dir.getVirtualFile() ) && JavaDirectoryService.getInstance().getPackage( dir ) != null )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
