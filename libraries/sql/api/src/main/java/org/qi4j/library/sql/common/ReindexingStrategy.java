/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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


package org.qi4j.library.sql.common;

import org.qi4j.api.service.ServiceComposite;

/**
 * Service to decide when complete re-indexing is needed, along with default implementations.
 * TODO maybe add {@code void beganReindexing()} and {@code void finishedReindexing()} methods? For example, for UI interaction.
 * 
 * You can decide your application's re-indexing policy for example like this:
 * {@code mainModule.addServices(ReindexingStrategy.ReindexingStrategyService.class).withMixins(ReindexingStrategy.AlwaysNeed.class);}.
 * This way your application will always re-build all index data at startup.
 *
 * @author Stanislav Muhametsin
 */
public interface ReindexingStrategy
{
   Boolean reindexingNeeded(String dbAppVersion, String currentAppVersion);
   
   /**
    * Define no mixins - let the application take care of that with {@link org.qi4j.bootstrap.ServiceDeclaration#withMixins(Class...)} method.
    *
    * @author Stanislav Muhametsin
    */
   public interface ReindexingStrategyService extends ReindexingStrategy, ServiceComposite
   {
      
   }
   
   /**
    * The re-indexing strategy which ALWAYS re-indexes everything. Useful with tests and during early development stage of application.
    *
    * @author Stanislav Muhametsin
    */
   public abstract class AlwaysNeed implements ReindexingStrategy
   {
      @Override
      public Boolean reindexingNeeded(String dbAppVersion, String currentAppVersion)
      {
         return true;
      }
   }
   
   /**
    * The re-indexing strategy which NEVER re-indexes anything. Useful when deleting and re-reading data is something to be avoided at all costs.
    *
    * @author Stanislav Muhametsin
    */
   public abstract class NeverNeed implements ReindexingStrategy
   {
      @Override
      public Boolean reindexingNeeded(String dbAppVersion, String currentAppVersion)
      {
         return false;
      }
   }
   
   /**
    * The re-indexing strategy, which re-indexes everything when application version changes. Useful when migration from versions is implemented,
    * or when application structure changes along with version (but not during same version).
    *
    * @author Stanislav Muhametsin
    */
   public abstract class NeedOnChange implements ReindexingStrategy
   {
      @Override
      public Boolean reindexingNeeded(String dbAppVersion, String currentAppVersion)
      {
         return !dbAppVersion.equals(currentAppVersion);
      }
   }
   
}
