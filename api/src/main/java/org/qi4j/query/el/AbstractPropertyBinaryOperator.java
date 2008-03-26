/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.query.el;

/**
 * TODO Add JavaDoc
 */
public class AbstractPropertyBinaryOperator<T>
    extends AbstractNotNullBinaryOperator<PropertyExpression, ValueExpression<T>>
    implements BooleanExpression
{

    /**
     * Constructor.
     *
     * @param property left side property expression; cannot be null
     * @param value    right side value expression; cannot be null
     * @throws IllegalArgumentException - If property or value are null
     */
    public AbstractPropertyBinaryOperator( final PropertyExpression property,
                                           final ValueExpression<T> value )
    {
        super( "Property expression", property, "Value expression", value );
    }

}