/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.dci;

import java.util.Stack;

/**
 * Maintains a stack of active contexts. Lookup finds the closest context of the given type on the stack.
 */
public class Contexts
{
   private static ThreadLocal<Stack<Object>> contexts = new ThreadLocal<Stack<Object>>()
   {
      @Override
      protected Stack<Object> initialValue()
      {
         return new Stack<Object>();
      }
   };

   private static Stack<Object> current()
   {
      return contexts.get();
   }

   // Access a context of a particular type on the stack
   public static <T> T context( Class<T> contextClass )
   {
      for (int i = current().size() - 1; i >= 0; i--)
         if (contextClass.isInstance( current().get( i ) ))
            return (T) current().get( i );

      throw new IllegalArgumentException( "No " + contextClass.getName() + " on the stack" );
   }

   // Perform a query with the given context
   public static <T, CONTEXT, EX extends Throwable> T withContext( CONTEXT context, Query<T, CONTEXT, EX> enactment ) throws EX
   {
      current().push( context );
      try
      {
         return enactment.query( context );
      } finally
      {
         current().pop();
      }
   }

   // Perform a command with the given context
   public static <CONTEXT, EX extends Throwable> void withContext( CONTEXT context, Command<CONTEXT, EX> enactment ) throws EX
   {
      current().push( context );
      try
      {
         enactment.command( context );
      } finally
      {
         current().pop();
      }
   }

   // Query enactments must implement this interface
   public interface Query<T, CONTEXT, EX extends Throwable>
   {
      T query( CONTEXT context )
            throws EX;
   }

   // Command enactments must implement this interface
   public interface Command<CONTEXT, EX extends Throwable>
   {
      void command( CONTEXT context )
            throws EX;
   }

}
