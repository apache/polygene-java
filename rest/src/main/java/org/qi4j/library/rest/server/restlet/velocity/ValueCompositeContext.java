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
import org.qi4j.api.Qi4j;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.StateDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class ValueCompositeContext
   extends AbstractContext
{
   private ValueComposite composite;

   public ValueCompositeContext( ValueComposite composite )
   {
      this.composite = composite;
   }

   @Override
   public Object internalGet( String s )
   {
      Property<Object> property = getProperty( s );

      if (property == null)
         return null;

      Object result = property.get();

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
      getProperty( s ).set( o );
      return o;
   }

   @Override
   public boolean internalContainsKey( Object o )
   {
      return getProperty( o.toString() ) != null;
   }

   @Override
   public Object[] internalGetKeys()
   {
      final List keys = new ArrayList();

       for( PropertyDescriptor propertyDescriptor : ((StateDescriptor)Qi4j.DESCRIPTOR_FUNCTION.map( composite )).properties() )
       {
           keys.add( propertyDescriptor.qualifiedName().name() );
       }

      return keys.toArray();
   }

   @Override
   public Object internalRemove( Object o )
   {
      getProperty( o.toString() ).set( o );
      return null;
   }

   private Property<Object> getProperty( String s )
   {
      PropertyDescriptor descriptor = ( (StateDescriptor) Qi4j.DESCRIPTOR_FUNCTION.map( composite ) ).getPropertyByName( s );
       if (descriptor == null)
           return null;

      return Qi4j.INSTANCE_FUNCTION.map( composite ).state().propertyFor( descriptor.accessor() );
   }

   @Override
   public String toString()
   {
      return composite.toString();
   }
}