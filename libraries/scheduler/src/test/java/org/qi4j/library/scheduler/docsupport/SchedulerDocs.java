package org.qi4j.library.scheduler.docsupport;

import org.qi4j.api.association.Association;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Property;
import org.qi4j.library.scheduler.Scheduler;
import org.qi4j.library.scheduler.Task;
import org.qi4j.library.scheduler.schedule.Schedule;
import org.qi4j.library.scheduler.timeline.Timeline;


public class SchedulerDocs
{

// START SNIPPET: timeline
    @Service Timeline timeline;
// END SNIPPET: timeline

// START SNIPPET: 2
    @Service Scheduler scheduler;

    public void method()
    {
        MyTaskEntity myTask = todo();
        Schedule schedule = scheduler.scheduleOnce( myTask, 10, false ); // myTask will be run in 10 seconds from now
    }

// END SNIPPET: 2
    MyTaskEntity todo() {
        return null;
    }

// START SNIPPET: 1
    interface MyTaskEntity extends Task, EntityComposite
    {

        Property<String> myTaskState();

        Association<AnotherEntity> anotherEntity();
    }

    abstract class MyTaskMixin implements Runnable
    {
        @This MyTaskEntity me;

        public void run()
        {
            me.myTaskState().set(me.anotherEntity().get().doSomeStuff(me.myTaskState().get()));
        }
    }

// END SNIPPET: 1
    interface AnotherEntity extends EntityComposite
    {
        String doSomeStuff(String p);
    }

}