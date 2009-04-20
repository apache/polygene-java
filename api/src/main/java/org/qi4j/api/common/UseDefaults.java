/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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
package org.qi4j.api.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote that the initial value of a Property will be the default value for the type if none is
 * specified during construction.
 * <br/>
 * These are the default values used for various types:
 * <pre>
 * Byte: 0
 * Short: 0
 * Character: 0
 * Integer: 0
 * Long: 0L
 * Double: 0D
 * Float: 0F
 * Boolean: false
 * String: ""
 * List: empty java.util.ArrayList
 * Set: empty java.util.HashSet
 * Collection: empty java.util.ArrayList
 * enum: first declared value
 * </pre>
 * If this annotation is not used, the property will be set to null.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )
@Documented
public @interface UseDefaults
{
}