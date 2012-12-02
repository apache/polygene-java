package org.qi4j.spi.entitystore.helpers;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.service.ServiceReference;

/**
 * Activation for JSONMapEntityStoreMixin.
 */
@Activators( JSONMapEntityStoreActivation.Activator.class )
public interface JSONMapEntityStoreActivation
{

    void setUpJSONMapES()
        throws Exception;

    void tearDownJSONMapES()
        throws Exception;

    /**
     * JSONMapEntityStoreMixin Activator.
     */
    public class Activator
        extends ActivatorAdapter<ServiceReference<JSONMapEntityStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<JSONMapEntityStoreActivation> activated )
            throws Exception
        {
            activated.get().setUpJSONMapES();
        }

        @Override
        public void beforePassivation( ServiceReference<JSONMapEntityStoreActivation> passivating )
            throws Exception
        {
            passivating.get().tearDownJSONMapES();
        }

    }

}
