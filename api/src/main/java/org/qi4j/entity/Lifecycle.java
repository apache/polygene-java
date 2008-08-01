/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.entity;

import org.qi4j.composite.Mixins;

/**
 * Lifecycle interface for all Composites.
 * <p/>
 * This Lifecycle interface is a built-in feature of the Qi4j runtime, which will establish
 * any Invocation stack against Lifecycle.class, but the Composite interface should never expose
 * it to client code.
 * <p/>
 * Example;
 * <code><pre>
 * public interface System
 * {
 *     User getAdmin();
 *
 *     void setAdmin( User admin );
 *
 * }
 *
 * public class SystemImpl
 *     implements System
 * {
 *     private User admin;
 *
 *     public User getAdmin()
 *     {
 *         return admin;
 *     }
 *
 *     public void setAdmin( User admin )
 *     {
 *         this.admin = admin;
 *     }
 * }
 *
 * public class SystemAdminLifecycleConcern extends ConcernOf<LifeCycle>
 *     implements Lifecyle
 * {
 *      &#64;Structure private UnitOfWork unit;
 *      &#64;This private Identity meAsIdentity;
 *      &#64;This private System meAsSystem;
 *
 *      public void create()
 *      {
 *          String thisId = meAsIdentity.identity().get();
 *          CompositeBuilder builder = unit.newEntityBuilder( thisId + ":1", UserComposite.class );
 *          User admin = builder.newInstance();
 *          meAsSystem.setAdmin( admin );
 *          next.create();
 *      }
 *
 *      public void remove()
 *      {
 *          next.remove();
 *          repository.deleteInstance( meAsSystem.getAdmin() );
 *      }
 * }
 *
 * &#64;Concerns( SystemAdminLifecycleModifier.class )
 * public interface SystemComposite extends System, Composite
 * {}
 *
 * </pre></code>
 */
@Mixins( Lifecycle.LifecycleMixin.class )
public interface Lifecycle
{

    /**
     * Creation callback method.
     * <p/>
     * Called by the Qi4j runtime before the newInstance of the composite completes, allowing
     * for additional initialization.
     */
    void create();

    /**
     * Removal callback method.
     * <p/>
     * Called by the Qi4j runtime before the composite is removed from the system, allowing
     * for clean-up operations.
     */
    void remove();

    // Default implementation
    public final class LifecycleMixin
        implements Lifecycle
    {
        public static final Lifecycle INSTANCE = new LifecycleMixin();

        public void create()
        {
        }

        public void remove()
        {
        }
    }
}
