package org.qi4j.library.alarm;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;

@Mixins(AlarmMixin.class)
public interface AlarmEntity extends Alarm, EntityComposite
{
}
