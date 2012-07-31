package org.qi4j.samples.forum.service;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.samples.forum.data.entity.Forums;
import org.qi4j.samples.forum.data.entity.Users;

/**
 * TODO
 */
@Mixins( BootstrapData.Mixin.class )
@Activators( BootstrapData.Activator.class )
public interface BootstrapData
    extends ServiceComposite
{
    
    void insertInitialData()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<BootstrapData>>
    {

        @Override
        public void afterActivation( ServiceReference<BootstrapData> activated )
                throws Exception
        {
            activated.get().insertInitialData();
        }

    }
    
    abstract class Mixin
        implements BootstrapData
    {
        @Structure
        Module module;

        @Override
        public void insertInitialData()
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

    }
}
