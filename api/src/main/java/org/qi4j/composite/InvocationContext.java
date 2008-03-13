/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.composite;

import java.util.Collection;

/**
 * The InvocationContext is available for Concerns to introspect during a method invocation.
 * It contains information about the invoked Composite and the Mixin that is to be invoked.
 * <p/>
 * It can also be used to access the Constraint violations that have been accumulated.
 */
public interface InvocationContext
{
    Object getComposite();

    Object getMixin();

    Class getMixinType();

    Collection<ConstraintViolation> getConstraintViolations();
}
