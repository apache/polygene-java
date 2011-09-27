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

package org.qi4j.library.rest.server.restlet.velocity;

import org.apache.velocity.context.AbstractContext;
import org.qi4j.api.value.ValueComposite;

import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
public class ListContext
      extends AbstractContext
      implements Iterable
{
   List list;

   ListContext( List list )
   {
      this.list = list;
   }

   @Override
   public Object internalGet( String s )
   {
      Object result = list.get( Integer.parseInt( s ) );

      if (result instanceof ValueComposite)
      {
         result = new ValueCompositeContext( (ValueComposite) result );
      } else if (result instanceof List)
      {
         result = new ListContext( (List) result );
      }

      return result;
   }

   @Override
   public Object internalPut( String s, Object o )
   {
      return list.set( Integer.parseInt( s ), o );
   }

   @Override
   public boolean internalContainsKey( Object o )
   {
      return list.get( Integer.parseInt( o.toString() ) ) != null;
   }

   @Override
   public Object[] internalGetKeys()
   {
      String[] indices = new String[list.size()];
      for (int i = 0; i < indices.length; i++)
      {
         indices[i] = Integer.toString( i );
      }

      return indices;
   }

   @Override
   public Object internalRemove( Object o )
   {
      return list.remove( Integer.parseInt( o.toString() ) );
   }

   @Override
   public String toString()
   {
      return list.toString();
   }

   public Iterator iterator()
   {
      return new Iterator()
      {
         int idx = 0;

         public boolean hasNext()
         {
            return idx < list.size();
         }

         public Object next()
         {
            Object result = list.get( idx++ );

            if (result instanceof ValueComposite)
            {
               result = new ValueCompositeContext( (ValueComposite) result );
            } else if (result instanceof List)
            {
               result = new ListContext( (List) result );
            }

            return result;
         }

         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      };
   }
}