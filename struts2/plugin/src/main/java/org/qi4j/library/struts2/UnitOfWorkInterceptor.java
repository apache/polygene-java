package org.qi4j.library.struts2;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * <p>An interceptor to be used to start a UnitOfWork if one has not yet been started.  If this interceptor creates a
 * new UnitOfWork it will also add a PreResultListener to make sure the unit of work is completed or discarded before
 * the result is executed.  This is important so that the rendering of the result does not make any changes to the
 * entities it is dealing with (we can maybe make this an option, to keep the UnitOfWork open until the invocation stack
 * returns to this interceptor or if it should use the PreResultListener, but for now I like the PreResultListener
 * better as it is safer).</p>
 *
 * <p>When a new UnitOfWork is created by this interceptor a decision must be made when closing it whether to discard
 * the UnitOfWork or complete it.  For now, if the resultCode is "success", we complete the UnitOfWork, otherwise we
 * discard it.  There are many things we can do here to make it more flexible, e.g. allow the user to specify result
 * codes to complete or, conversely, codes that should result in the UnitOfWork being discarded.  Since we have access
 * to the action invocation we could also do something with annotations on the method to be executed to allow user to
 * specify more specifically for that method what results should result in discards or completions.</p>
 */
public class UnitOfWorkInterceptor
    extends AbstractInterceptor
{

    private static final long serialVersionUID = 1L;

    @Structure
    private UnitOfWorkFactory uowf;

    @Override
    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        boolean createdUnitOfWork = false;
        UnitOfWork uow = uowf.currentUnitOfWork();
        if( uow == null )
        {
            uow = uowf.newUnitOfWork();
            createdUnitOfWork = true;
        }

        String resultCode = null;
        try
        {
            resultCode = invocation.invoke();
        }
        finally
        {
            if( createdUnitOfWork && uow.isOpen() )
            {
                if( shouldComplete( invocation, resultCode ) )
                {
                    uow.complete();
                }
                else
                {
                    uow.discard();
                }
            }
        }
        return resultCode;
    }

    protected boolean shouldComplete( ActionInvocation invocation, String resultCode )
    {
        return Action.SUCCESS.equals( resultCode );
    }
}
