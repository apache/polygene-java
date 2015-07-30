/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.ide.plugin.idea.concerns.actions.create;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.apache.zest.ide.plugin.idea.common.actions.AbstractCreateElementActionBase;

/**
 * @author edward.yakop@gmail.com
 */
public class CreateConcernFromMixinTypeOrCompositeAction extends AbstractCreateElementActionBase
{
    public CreateConcernFromMixinTypeOrCompositeAction()
    {
        super( "TODO", "TODO" );
    }

    protected String getCommandName()
    {
        return "CreateConcernFromMixinTypeOrCompositeAction";
    }

    protected String getActionName( PsiDirectory directory, String newName )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected String getDialogPrompt()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected String getDialogTitle()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    protected PsiElement[] create( String newName, PsiDirectory directory )
        throws Exception
    {
        return new PsiElement[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
