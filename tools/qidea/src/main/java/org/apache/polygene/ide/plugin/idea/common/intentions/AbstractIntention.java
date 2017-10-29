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

package org.apache.polygene.ide.plugin.idea.common.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.apache.polygene.ide.plugin.idea.common.resource.PolygeneResourceBundle.message;

/**
 * This class is based from {@code com.siyeh.ipp.base.Intention}
 *
 */
public abstract class AbstractIntention extends PsiElementBaseIntentionAction
{
    protected abstract boolean isIntentionValidFor( PsiElement element );

    protected abstract String resourceBundlePrefixId();

    /**
     * Implement this method to process intention.
     *
     * @param project The project in which the availability is checked.
     * @param editor  The editor in which the intention will be invoked.
     * @param element The element under caret.
     */
    protected abstract void processIntention( @NotNull Project project,
                                              @NotNull Editor editor,
                                              @NotNull PsiElement element );

    public void invoke( @NotNull Project project, Editor editor, PsiFile file )
        throws IncorrectOperationException
    {
        if( isFileReadOnly( project, file ) )
        {
            return;
        }

        final PsiElement element = findMatchingElement( file, editor );
        if( element == null )
        {
            return;
        }

        processIntention( project, editor, element );
    }

    protected static boolean isFileReadOnly( @NotNull Project project, @NotNull PsiFile file )
    {
        VirtualFile virtualFile = file.getVirtualFile();
        ReadonlyStatusHandler readonlyStatusHandler = ReadonlyStatusHandler.getInstance( project );
        ReadonlyStatusHandler.OperationStatus operationStatus =
            readonlyStatusHandler.ensureFilesWritable( virtualFile );
        return operationStatus.hasReadonlyFiles();
    }

    @Nullable
    private PsiElement findMatchingElement( @NotNull PsiFile file, @NotNull Editor editor )
    {
        CaretModel caretModel = editor.getCaretModel();
        int position = caretModel.getOffset();
        PsiElement element = file.findElementAt( position );
        return findMatchingElement( element );
    }

    @Nullable
    private PsiElement findMatchingElement( @Nullable PsiElement element )
    {
        while( element != null )
        {
            if( isIntentionValidFor( element ) )
            {
                return element;
            }
            else
            {
                element = element.getParent();
                if( element instanceof PsiFile )
                {
                    break;
                }
            }
        }

        return null;
    }

    @Override
    public boolean isAvailable( @NotNull Project project, Editor editor, @Nullable PsiElement element )
    {
        return isIntentionValidFor( element );
    }

    @NotNull
    public final String getFamilyName()
    {
        return message( resourceBundlePrefixId() + ".family.name" );
    }

    @NotNull
    @Override
    public final String getText()
    {
        return message( resourceBundlePrefixId() + ".name" );
    }
}
