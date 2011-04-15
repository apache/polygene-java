package org.qi4j.ide.plugin.idea.concerns.actions.create;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.qi4j.ide.plugin.idea.common.actions.AbstractCreateElementActionBase;

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
