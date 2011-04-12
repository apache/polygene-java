package org.qi4j.library.alarm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

@Mixins( AlarmStatusFactory.Mixin.class )
public interface AlarmStatusFactory
    extends ServiceComposite
{
    AlarmStatus createStatus( String systemName );

    abstract class Mixin
        implements AlarmStatusFactory
    {
        @Service
        private Iterable<AlarmModel> models;

        @Structure
        private ValueBuilderFactory vbf;

        @Override
        public AlarmStatus createStatus( String systemName )
        {
            ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder(AlarmStatus.class);
            AlarmStatus prototype = builder.prototype();
            prototype.name().set(systemName);
            prototype.creationDate().set( new Date() );
            return builder.newInstance();
        }
    }
}
