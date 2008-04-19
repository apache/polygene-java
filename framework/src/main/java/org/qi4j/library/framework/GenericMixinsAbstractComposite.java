/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.framework;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.association.AssociationMixin;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.framework.scripting.JRubyMixin;
import org.qi4j.library.framework.scripting.JavaScriptMixin;
import org.qi4j.library.framework.scripting.GroovyMixin;
import org.qi4j.property.PropertyMixin;

/**
 * Adds all the various generic mixins that can be used to implement
 * Composite methods.
 */
@Mixins( { PropertyMixin.class, AssociationMixin.class, PropertiesMixin.class, JavaScriptMixin.class, JRubyMixin.class, GroovyMixin.class, RMIMixin.class, NoopMixin.class } )
public interface GenericMixinsAbstractComposite
{
}
