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

package org.qi4j.api.mixin;

/**
 * Fragments which want to be initialized can implement
 * this callback interface. It will be invoked after
 * the fragment has bee instantiated and all injections have been done.
 */
public interface Initializable
{
    /**
     * Initialize the fragment
     *
     * @throws org.qi4j.api.mixin.InitializationException
     *          if something went wrong
     */
    void initialize()
        throws InitializationException;
}
