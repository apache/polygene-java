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
package org.qi4j.ide.plugin.idea.common.actions;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import java.util.Properties;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public abstract class AbstractCreateElementActionBase extends CreateElementActionBase
{
    @NonNls
    private static final String NAME_TEMPLATE_PROPERTY = "NAME";

    protected AbstractCreateElementActionBase( String text, String description )
    {
        super( text, description, null );
    }

    @NotNull
    protected final PsiElement[] invokeDialog( Project project, PsiDirectory directory )
    {
        Module module = ModuleUtil.findModuleForFile( directory.getVirtualFile(), project );
        if( module == null )
        {
            return PsiElement.EMPTY_ARRAY;
        }

        MyInputValidator validator = doInvokeDialog( project, directory );
        return validator.getCreatedElements();
    }

    protected MyInputValidator doInvokeDialog( Project project, PsiDirectory directory )
    {
        MyInputValidator validator = new MyInputValidator( project, directory );
        Messages.showInputDialog( project, getDialogPrompt(), getDialogTitle(), Messages.getQuestionIcon(), "", validator );
        return validator;
    }

    /**
     * @return Dialog prompt.
     */
    protected abstract String getDialogPrompt();

    /**
     * @return Dialog title.
     */
    protected abstract String getDialogTitle();

    protected String getErrorTitle()
    {
        return CommonBundle.getErrorTitle();
    }

    protected final void checkBeforeCreate( String newName, PsiDirectory directory )
        throws IncorrectOperationException
    {
        JavaDirectoryService javaDirectoryService = JavaDirectoryService.getInstance();
        javaDirectoryService.checkCreateClass( directory, newName );
    }

    protected static PsiClass createClassFromTemplate( @NotNull PsiDirectory directory,
                                                       @NotNull String className,
                                                       @NotNull String templateName,
                                                       @NonNls String... parameters )
        throws IncorrectOperationException
    {
        String classFileName = className + "." + StdFileTypes.JAVA.getDefaultExtension();
        PsiFile file = createFromTemplateInternal( directory, className, classFileName, templateName, parameters );
        return ( (PsiJavaFile) file ).getClasses()[ 0 ];
    }

    protected static PsiFile createFromTemplateInternal( @NotNull PsiDirectory directory,
                                                         @NotNull String name,
                                                         @NotNull String fileName,
                                                         @NotNull String templateName,
                                                         @NonNls String... parameters )
        throws IncorrectOperationException
    {
        // Load template
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance();
        FileTemplate template = fileTemplateManager.getJ2eeTemplate( templateName );

        // Process template properties
        Properties properties = new Properties( fileTemplateManager.getDefaultProperties() );
        JavaTemplateUtil.setPackageNameAttribute( properties, directory );
        properties.setProperty( NAME_TEMPLATE_PROPERTY, name );

        // Add parameters
        for( int i = 0; i < parameters.length; i += 2 )
        {
            properties.setProperty( parameters[ i ], parameters[ i + 1 ] );
        }

        // Create text from template with specified properties
        String text;
        try
        {
            text = template.getText( properties );
        }
        catch( Exception e )
        {
            String message = "Unable to load template for " +
                             fileTemplateManager.internalTemplateToSubject( templateName );
            throw new RuntimeException( message, e );
        }

        // Serialized text to file
        PsiManager psiManager = PsiManager.getInstance( directory.getProject() );
        PsiFileFactory fileFactory = PsiFileFactory.getInstance( directory.getProject() );
        PsiFile file = fileFactory.createFileFromText( fileName, text );

        // Reformat the file according to project/default style
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance( psiManager );
        codeStyleManager.reformat( file );

        // Add newly created file to directory
        return (PsiFile) directory.add( file );
    }
}
