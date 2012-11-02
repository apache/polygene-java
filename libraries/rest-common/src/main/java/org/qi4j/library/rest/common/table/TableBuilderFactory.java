/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
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

package org.qi4j.library.rest.common.table;

import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.functional.Function;

/**
 * Factory for setting up TableBuilder instances. Defines available columns, and how they are extracted from objects.
 */
public class TableBuilderFactory
{
   private ValueBuilderFactory vbf;

   public TableBuilderFactory(ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
   }

   private Map<String, Column> columns = new LinkedHashMap<String, Column>();

   public TableBuilderFactory column(String id, String label, String type, Function<?, Object> valueFunction, Function<?, String> formattedFunction)
   {
      columns.put(id, new Column(id, label, type, valueFunction, formattedFunction));
      return this;
   }

   public TableBuilderFactory column(String id, String label, String type, Function<?, Object> valueFunction)
   {
      columns.put(id, new Column(id, label, type, valueFunction, null));
      return this;
   }

   public TableBuilder newInstance(TableQuery tableQuery)
   {
      return new TableBuilder(vbf, columns, tableQuery);
   }

   class Column
   {
      private String id;
      private String label;
      private String type;
      private Function<?, Object> valueFunction;
      private Function<?, String> formattedFunction;

      private Column(String id, String label, String type, Function<?, Object> valueFunction, Function<?, String> formattedFunction)
      {
         this.id = id;
         this.label = label;
         this.type = type;
         this.valueFunction = valueFunction;
         this.formattedFunction = formattedFunction;
      }

      public String getId()
      {
         return id;
      }

      public String getLabel()
      {
         return label;
      }

      public String getType()
      {
         return type;
      }

      public Function<?, Object> getValueFunction()
      {
         return valueFunction;
      }

      public Function<?, String> getFormattedFunction()
      {
         return formattedFunction;
      }
   }
}
