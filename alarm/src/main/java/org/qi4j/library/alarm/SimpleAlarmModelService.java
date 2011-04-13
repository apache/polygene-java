package org.qi4j.library.alarm;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

@Mixins( SimpleAlarmModelMixin.class)
public interface SimpleAlarmModelService extends AlarmModel, ServiceComposite
{
}
