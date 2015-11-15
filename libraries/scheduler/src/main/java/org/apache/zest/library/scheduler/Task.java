/*
 * Copyright (c) 2010-2012, Paul Merlin.
 * Copyright (c) 2012, Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.scheduler;

import java.util.List;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;

/**
 * Compose an Entity using this type to be able to Schedule it.
 *<p>
 * A Task is associated from a {@link Schedule}, and upon time to execute
 * the SchedulerService will dispatch a TaskRunner in a new thread, and establish a UnitOfWork (Usecase name of "Task Runner").
 *</p>
 *<p>
 * The {@code Task} type declares the {@link UnitOfWorkConcern} and therefor the {@code Task} implementation may
 * declare the {@link UnitOfWorkPropagation} annotation with the
 * {@link org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation.Propagation#REQUIRES_NEW} and a different
 * {@link UnitOfWork} strategy, such as {@code Retries} and {@code DiscardOn}.
 *
 *</p>
 *
 * Here is a simple example:
 * <pre><code>
 *  interface MyTask
 *      extends Task
 *  {
 *      Property&lt;String customState();
 *      Association&lt;AnotherEntity&gt; anotherEntity();
 *  }
 *
 *  class MyTaskMixin
 *      implements Runnable
 *  {
 *      &#64;This MyTaskEntity me;
 *
 *      public void run()
 *      {
 *          me.customState().set( me.anotherEntity().get().doSomeStuff( me.customState().get() ) );
 *      }
 *  }
 * </code></pre>
 *
 * Finaly, {@literal MyTask} must be assembled into an {@literal EntityComposite}.
 */
// START SNIPPET: task
@Concerns( UnitOfWorkConcern.class )
public interface Task
    extends Runnable
{
    Property<String> name();

    @UseDefaults
    Property<List<String>> tags();

}
// END SNIPPET: task
