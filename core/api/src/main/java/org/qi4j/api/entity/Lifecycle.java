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
package org.qi4j.api.entity;

/**
 * Lifecycle interface for all Composites.
 * <p/>
 * This Lifecycle interface is a built-in feature of the Qi4j runtime, similar to the Initializable interface.
 * Any Mixin that implements this interface AND is part of an EntityComposite will have these two methods called
 * upon creation/removal of the EntityComposite instance to/from the EntityStore. Meaning, the create method is called
 * only when the identifiable EntityComposite is created the first time, and not when it is read from its persisted
 * state and created into memory.
 * <p/>
 * Example;
 * <code><pre>
 * public interface System
 * {
 *     Property&lt;User&gt; admin();
 * }
 *
 * public class SystemAdminMixin<LifeCycle>
 *     implements System, Lifecyle, ...
 * {
 *      &#64;Structure private UnitOfWork uow;
 *      &#64;This private Identity meAsIdentity;
 *
 *      public void create()
 *      {
 *          String thisId = meAsIdentity.identity().get();
 *          EntityBuilder builder = uow.newEntityBuilder( thisId + ":1", UserComposite.class );
 *          User admin = builder.newInstance();
 *          admin.set( admin );
 *      }
 *
 *      public void remove()
 *      {
 *          uow.remove( admin.get() );
 *      }
 * }
 *
 * &#64;Mixins( SystemAdminMixin.class )
 * public interface SystemEntity extends System, EntityComposite
 * {}
 *
 * </pre></code>
 */
public interface Lifecycle
{

    /**
     * Creation callback method.
     * <p/>
     * Called by the Qi4j runtime before the newInstance of the entity completes, before the constraints are checked,
     * allowing for additional initialization.
     *
     * @throws LifecycleException if the entity could not be created
     */
    void create()
        throws LifecycleException;

    /**
     * Removal callback method.
     * <p/>
     * Called by the Qi4j runtime before the entity is removed from the system, allowing
     * for clean-up operations.
     *
     * @throws LifecycleException if the entity could not be removed
     */
    void remove()
        throws LifecycleException;
}
