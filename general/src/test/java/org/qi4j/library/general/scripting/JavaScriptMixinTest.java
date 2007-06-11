package org.qi4j.library.general.scripting;
/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/

import junit.framework.*;
import org.qi4j.library.general.scripting.JavaScriptMixin;
import org.qi4j.library.general.scripting.ScriptComposite;
import org.qi4j.api.ObjectFactory;
import org.qi4j.runtime.ObjectFactoryImpl;
import org.qi4j.test.model.Mixin1;

public class JavaScriptMixinTest extends TestCase
{
    JavaScriptMixin scriptMixin;

    public void testInvoke() throws Throwable
    {
        ObjectFactory factory = new ObjectFactoryImpl();

        Mixin1 domain = factory.newInstance( ScriptComposite.class);

        System.out.println(domain.do1());
    }
}