/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.tutorials.composites.tutorial10;

import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.mixin.Mixins;

// START SNIPPET: solution

/**
 * This Composite interface declares transitively all the Fragments of the HelloWorld composite.
 * <p>
 * The Fragments are all abstract, so it's ok to
 * put the domain methods here. Otherwise the Fragments
 * would have to implement all methods, including those in Composite.
 * </p>
 */
@Mixins( { HelloWorldMixin.class } )
public interface HelloWorldComposite
    extends TransientComposite
{
    String say();
}
// END SNIPPET: solution
