/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map objects to role interfaces. An instance of RoleMap
 * is created for each request, and is sent down in the interactions chain
 * by InteractionMixin.subContext. Whenever an object is identified in the chain,
 * add it to the roleMap.
 *
 * If an object has been added under a general role, then you can map it to a more specific
 * role by using the map() method.
 *
 * If objects are registered under their generic types, and a role lookup does not match anything, the
 * objects will be searched in the reverse insertion order, i.e. last registered object will be checked first.
 */
public class RoleMap
{
   private static ThreadLocal<RoleMap> currentRoleMap = new ThreadLocal<RoleMap>();

   public static RoleMap current()
   {
      return currentRoleMap.get();
   }

   public static <T> T role(Class<T> roleClass)
   {
      RoleMap map = currentRoleMap.get();
      if (map == null)
         throw new IllegalStateException("No current role map");

      return map.get( roleClass );
   }

   public static void setCurrentRoleMap(RoleMap roleMap)
   {
      currentRoleMap.set( roleMap );
   }

   public static void newCurrentRoleMap()
   {
      currentRoleMap.set( new RoleMap() );
   }

   public static void clearCurrentRoleMap()
   {
      currentRoleMap.remove();
   }

   private Map<Class, Object> roles = new HashMap<Class, Object>( );

   private RoleMap parentRoleMap;

   /**
    * Create new root roleMap
    */
   public RoleMap()
   {
   }

   public RoleMap( RoleMap parentRoleMap )
   {
      this.parentRoleMap = parentRoleMap;
   }

   public void set(Object object, Class... roleClasses)
   {
      if (object == null)
      {
         for (Class roleClass : roleClasses)
         {
            roles.remove( roleClass );
         }
      } else
      {
         if (roleClasses.length == 0)
         {
            roles.put( object.getClass(), object );
         } else
         {
            for (Class roleClass : roleClasses)
            {
               roles.put( roleClass, object );
            }
         }
      }
   }

   public <T> T get(Class<T> roleClass) throws IllegalArgumentException
   {
      Object object = roles.get( roleClass );

      if (object == null)
      {
         // If no explicit mapping has been made, see if
         // any other mapped objects could work
         for (Map.Entry<Class, Object> entry : roles.entrySet())
         {
            if (roleClass.isAssignableFrom( entry.getKey()))
            {
               return roleClass.cast( entry.getValue() );
            }
         }
      } else
      {
         return roleClass.cast( object );
      }

      if (parentRoleMap == null)
         throw new IllegalArgumentException("No object in roleMap for role:"+roleClass.getSimpleName());
      else
         return parentRoleMap.get( roleClass );
   }

   /**
    * Get all mapped objects, from this and all parent contexts.
    *
    * @param roleClass role of objects to be returned
    * @return list of all objects in all stacked contexts that are mapped to the given role
    */
   public <T> List<T> getAll(Class<T> roleClass)
   {
      List<T> list = new ArrayList<T>( );

      getAll0(roleClass, list);

      return list;
   }

   private <T> void getAll0( Class<T> roleClass, List<T> list )
   {
      for (Object obj : roles.values())
      {
         if (roleClass.isInstance(obj) && !list.contains(obj))
            list.add((T) obj);
      }

      // Check parent roleMap
      if (parentRoleMap != null)
         parentRoleMap.getAll0( roleClass, list );
   }

   public void map(Class fromRoleClass, Class... toRoleClasses)
   {
      Object object = get(fromRoleClass);

      for (Class toRoleClass : toRoleClasses)
      {
         if (toRoleClass.isInstance( object))
            roles.put( toRoleClass, object );
         else
            throw new IllegalArgumentException(object +" does not implement role type "+toRoleClass.getName());
      }
   }
}
