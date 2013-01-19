package org.qi4j.manual.recipes.sideeffects;

import org.qi4j.api.sideeffect.SideEffectOf;

public class MailNotifySideEffect extends SideEffectOf<Order>
    implements Order
{
}
