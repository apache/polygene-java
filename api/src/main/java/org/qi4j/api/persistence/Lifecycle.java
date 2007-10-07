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
package org.qi4j.api.persistence;

import org.qi4j.api.annotation.Mixins;

/**
 * Lifecycle interface for all Composites.
 * <p/>
 * This Lifecycle interface is a built-in feature of the Qi4J runtime, which will establish
 * any Invocation stack against Lifecycle.class, but the Composite interface should never expose
 * it to client code.
 * <p/>
 * Example;
 * <code><pre>
 * public interface System
 * {
 *     User getAdmin();
 * <p/>
 *     void setAdmin( User admin );
 * <p/>
 * }
 * <p/>
 * public class SystemImpl
 *     implements System
 * {
 *     private User admin;
 * <p/>
 *     public User getAdmin()
 *     {
 *         return admin;
 *     }
 * <p/>
 *     public void setAdmin( User admin )
 *     {
 *         this.admin = admin;
 *     }
 * }
 * <p/>
 * public class SystemAdminLifecycleModifier
 *     implements Lifecyle
 * {
 *      @Modifies private Lifecycle next;
 *      @DependsOn private EntitySession session;
 *      @ThisAs private Identity meAsIdentity;
 *      @ThisAs private System meAsSystem;
 * <p/>
 *      public void create()
 *      {
 *          String thisId = meAsIdentity.getIdentity();
 *          CompositeBuilder builder = session.newEntityBuilder( thisId + ":1", UserComposite.class );
 *          User admin = builder.newInstance();
 *          meAsSystem.setAdmin( admin );
 *          next.create();
 *      }
 * <p/>
 *      public void delete()
 *      {
 *          next.delete();
 *          repository.deleteInstance( meAsSystem.getAdmin() );
 *      }
 * }
 *
 * @ModifedBy( SystemAdminLifecycleModifier.class )
 * public interface SystemComposite extends System, Composite
 * {}
 * <p/>
 * </pre></code>
 */
@Mixins( Lifecycle.LifecycleMixin.class )
public interface Lifecycle
{

    /**
     * Creation callback method.
     * <p/>
     * Called by the Qi4J runtime before the newInstance of the composite completes, allowing
     * for additional initialization.
     */
    void create();

    /**
     * Deletion callback method.
     * <p/>
     * Called by the Qi4J runtime before the composite is deleted from the system, allowing
     * for clean-up operations.
     */
    void delete();

    // Default implementation
    public final class LifecycleMixin
        implements Lifecycle
    {
        public static final Lifecycle INSTANCE = new LifecycleMixin();

        public void create()
            throws PersistenceException
        {
        }

        public void delete()
            throws PersistenceException
        {
        }
    }
}
