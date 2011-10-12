package org.qi4j.samples.forum.service;

import com.sun.org.apache.xml.internal.security.Init;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.samples.forum.data.entity.Forums;
import org.qi4j.samples.forum.data.entity.Users;

/**
 * TODO
 */
@Mixins(BootstrapData.Mixin.class)
public interface BootstrapData
    extends ServiceComposite, Activatable
{
    class Mixin
        implements Activatable
    {
        @Structure
        Module module;

        @Override
        public void activate()
            throws Exception
        {
            UnitOfWork unitOfWork = module.newUnitOfWork();

            try
            {
                unitOfWork.get( Forums.class, Forums.FORUMS_ID );
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.newEntity( Forums.class, Forums.FORUMS_ID );
            }

            try
            {
                unitOfWork.get( Users.class, Users.USERS_ID );
            }
            catch( NoSuchEntityException e )
            {
                unitOfWork.newEntity( Users.class, Users.USERS_ID );
            }

            try
            {
                unitOfWork.complete();
            }
            catch( UnitOfWorkCompletionException e )
            {
                throw new InitializationException( e );
            }
        }

        @Override
        public void passivate()
            throws Exception
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
