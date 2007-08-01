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

import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.persistence.impl.LifecycleImpl;

/** Lifecycle interface for all Composites.
 *
 * This Lifecycle interface is a built-in feature of the Qi4J runtime, which will establish
 * any Modifier stack against Lifecycle.class, but the Composite interface should never expose
 * it to client code.
 *
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
 *     public SystemImpl( User admin )
 *     {
 *         this.admin = admin;
 *     }
 *
 *     public User getAdmin()
 *     {
 *         return admin;
 *     }
 * }
 *
 * public class SystemAdminLifecycleModifier
 *     implements Lifecyle
 * {
 *      @Modifies private Lifecycle next;
 *      @Dependency private CompositeBuilder<System> builder;
 *      @Dependency private EntityRepository repository;
 *      @Uses private Identity meAsIdentity;
 *      @Uses private System meAsSystem;
 *
 *      public void create()
 *      {
 *          String thisId = meAsIdentity.getIdentity();
 *          User admin = repository.newInstance( thisId + ":1", UserComposite.class );
 *          builder.adapt( new SystemImpl( admin ) );
 *          next.create();
 *      }
 *
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
 *
 * </pre></code>
 */
public interface Lifecycle
{
    /** Creation callback method.
     *
     * Called by the Qi4J runtime before the newInstance of the composite completes, allowing
     * for additional initialization.
     */
    void create();

    /** Deletion callback method.
     *
     * Called by the Qi4J runtime before the composite is deleted from the system, allowing
     * for clean-up operations.
     */
    void delete();
}
