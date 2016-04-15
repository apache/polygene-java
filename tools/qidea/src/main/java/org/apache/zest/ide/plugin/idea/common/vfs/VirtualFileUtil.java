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

package org.apache.zest.ide.plugin.idea.common.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1
 */
public final class VirtualFileUtil
{
    /**
     * @param element element to process.
     * @return The containing virtual file of the element.
     * @since 0.1
     */
    @Nullable
    public static VirtualFile getVirtualFile( @NotNull PsiElement element )
    {
        if( element instanceof PsiFileSystemItem )
        {
            PsiFileSystemItem fileSystemItem = (PsiFileSystemItem) element;
            return fileSystemItem.getVirtualFile();
        }

        // If it's not a file system, assume that this is an element within a file
        PsiFile containingFile = element.getContainingFile();
        if( containingFile == null )
        {
            return null;
        }

        VirtualFile virtualFile = containingFile.getVirtualFile();
        if( virtualFile != null )
        {
            return virtualFile;
        }

        PsiFile originalFile = containingFile.getOriginalFile();
        if( originalFile == null )
        {
            return null;
        }

        return originalFile.getVirtualFile();
    }

    private VirtualFileUtil()
    {
    }
}
