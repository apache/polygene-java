package org.qi4j.manual.recipes.sideeffects;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.sideeffect.SideEffects;

// START SNIPPET: body
@SideEffects( MailNotifySideEffect.class )
public interface OrderEntity
    extends Order, HasSequenceNumber, HasCustomer,
            HasLineItems, Confirmable, EntityComposite
{
}
// END SNIPPET: body
