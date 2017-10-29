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

package org.apache.polygene.ide.plugin.idea.concerns.actions.create.inPackage;

import com.intellij.ide.actions.CreateInPackageActionBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.DataKeys.PROJECT;
import static com.intellij.openapi.actionSystem.DataKeys.PSI_ELEMENT;
import static com.intellij.util.Icons.CLASS_ICON;
import static org.apache.polygene.ide.plugin.idea.common.psi.search.GlobalSearchScopeUtil.determineSearchScope;
import static org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle.message;
import static org.apache.polygene.ide.plugin.idea.concerns.common.PolygeneConcernConstants.TEMPLATE_GENERIC_CONCERN_OF;
import static org.apache.polygene.ide.plugin.idea.concerns.common.PolygeneConcernUtil.getConcernOfClass;

/**
 * JAVADOC: Non generic concern
 *
 * @since 0.1
 */
public class CreateConcernOfInPackageAction extends CreateInPackageActionBase
{
    protected CreateConcernOfInPackageAction()
    {
        super( message( "createConcernOfInPackage.menu.action.text" ),
               message( "createConcernOfInPackage.menu.action.description" ),
               CLASS_ICON );
    }

    @Override
    protected final boolean isAvailable( DataContext dataContext )
    {
        boolean isAvailable = super.isAvailable( dataContext );
        if( !isAvailable )
        {
            return false;
        }

        PsiElement psiElement = PSI_ELEMENT.getData( dataContext );
        if( psiElement == null )
        {
            return false;
        }

        GlobalSearchScope searchScope = determineSearchScope( psiElement );
        if( searchScope == null )
        {
            return false;
        }

        Project project = PROJECT.getData( dataContext );
        PsiClass psiClass = getConcernOfClass( project, searchScope );
        return psiClass != null;
    }

    @NotNull
    protected final PsiElement[] invokeDialog( Project project, PsiDirectory directory )
    {
        MyInputValidator validator = new MyInputValidator( project, directory );
        Messages.showInputDialog( project, message( "createConcernOfInPackage.dlg.prompt" ),
                                  message( "createConcernOfInPackage.dlg.title" ),
                                  Messages.getQuestionIcon(), "", validator );
        return validator.getCreatedElements();
    }

    protected final String getCommandName()
    {
        return message( "createConcernOfInPackage.command.name" );
    }

    protected final String getErrorTitle()
    {
        return message( "createConcernOfInPackage.error.title" );
    }

    protected final String getActionName( PsiDirectory directory, String newName )
    {
        return message( "createConcernOfInPackage.progress.text", newName );
    }

    protected final void doCheckCreate( final PsiDirectory dir, final String className )
        throws IncorrectOperationException
    {
        JavaDirectoryService javaDirectoryService = JavaDirectoryService.getInstance();
        javaDirectoryService.checkCreateClass( dir, className );
    }

    @NotNull
    protected PsiClass doCreate( final PsiDirectory dir, final String className )
        throws IncorrectOperationException
    {
        JavaDirectoryService javaDirectoryService = JavaDirectoryService.getInstance();
        return javaDirectoryService.createClass( dir, className, TEMPLATE_GENERIC_CONCERN_OF );
    }
}
