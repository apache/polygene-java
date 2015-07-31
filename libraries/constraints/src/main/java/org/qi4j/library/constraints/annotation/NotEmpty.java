/*
 * Copyright 2008 Rickard Oberg
 * Copyright 2008 Richard Wallace
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
package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.NotEmptyCollectionConstraint;
import org.qi4j.library.constraints.NotEmptyStringConstraint;

/**
 * Marks a property as being a string or collection, non null, not empty.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@InstanceOf( { String.class, Collection.class } )
@Constraints( { NotEmptyStringConstraint.class, NotEmptyCollectionConstraint.class } )
public @interface NotEmpty
{
}
