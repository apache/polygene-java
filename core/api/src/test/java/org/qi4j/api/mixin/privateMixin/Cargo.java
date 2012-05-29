package org.qi4j.api.mixin.privateMixin;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;

// START SNIPPET: private
@Mixins( CargoMixin.class )
public interface Cargo extends EntityComposite
{
    String origin();

    String destination();

    void changeDestination( String newDestination );

}

// END SNIPPET: private
