/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.api.injection.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.api.injection.InjectionScope;

/**
 * Annotation to denote the injection of a property, association or
 * StateHolder.
 * <pre><code>
 * &#64;State Property<String> propertyName;
 * &#64;State Association<MyEntity> associationName;
 * &#64;State ManyAssociation<MyEntity> manyAssociationName;
 * &#64;State StateHolder state;
 * &#64;State AssociationStateHolder associationState;
 * </code></pre>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
@InjectionScope
public @interface State
{
    /**
     * Name of the property or association.
     * If not set then name will be name of field.
     *
     * @return the name
     */
    public abstract String value() default "";
}