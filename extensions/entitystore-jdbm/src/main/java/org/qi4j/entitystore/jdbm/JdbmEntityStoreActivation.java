package org.qi4j.entitystore.jdbm;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.service.ServiceReference;

@Activators( { JdbmEntityStoreActivation.Activator.class } )
public interface JdbmEntityStoreActivation
{

    void setUpJdbm()
            throws Exception;

    void tearDownJdbm()
            throws Exception;

    public class Activator
            extends ActivatorAdapter<ServiceReference<JdbmEntityStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<JdbmEntityStoreActivation> activated )
                throws Exception
        {
            activated.get().setUpJdbm();
        }

        @Override
        public void beforePassivation( ServiceReference<JdbmEntityStoreActivation> passivating )
                throws Exception
        {
            passivating.get().tearDownJdbm();
        }

    }

}
