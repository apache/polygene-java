/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.entity;

/**
 * Lifecycle interface for all Composites.
 * <p>
 * This Lifecycle interface is a built-in feature of the Polygene runtime, similar to the Initializable interface.
 * Any Mixin that implements this interface AND is part of an EntityComposite will have these two methods called
 * upon creation/removal of the EntityComposite instance to/from the EntityStore. Meaning, the create method is called
 * only when the identifiable EntityComposite is created the first time, and not when it is read from its persisted
 * state and created into memory.
 * </p>
 * <p>
 * Example;
 * </p>
 * <pre><code>
 * public interface System
 * {
 *     Property&lt;User&gt; admin();
 * }
 *
 * public class SystemAdminMixin&lt;LifeCycle&gt;
 *     implements System, Lifecyle, ...
 * {
 *      &#64;Structure private UnitOfWork uow;
 *      &#64;This private Identity meAsIdentity;
 *
 *      public void create()
 *      {
 *          String thisId = meAsIdentity.reference().get();
 *          EntityBuilder builder = uow.newEntityBuilder( thisId + ":1", UserComposite.class );
 *          User admin = builder.newInstance();
 *          admin().set( admin );
 *      }
 *
 *      public void remove()
 *      {
 *          uow.remove( admin().get() );
 *      }
 * }
 *
 * &#64;Mixins( SystemAdminMixin.class )
 * public interface SystemEntity extends System, EntityComposite
 * {}
 *
 * </code></pre>
 */
public interface Lifecycle
{
    /**
     * Creation callback method.
     * <p>
     * Called by the Polygene runtime before the newInstance of the entity completes, before the constraints are checked,
     * allowing for additional initialization.
     * </p>
     * @throws Exception if the entity could not be created
     */
    void create() throws Exception;

    /**
     * Removal callback method.
     * <p>
     * Called by the Polygene runtime before the entity is removed from the system, allowing
     * for clean-up operations.
     * </p>
     * @throws Exception if the entity could not be removed
     */
    void remove() throws Exception;
}
