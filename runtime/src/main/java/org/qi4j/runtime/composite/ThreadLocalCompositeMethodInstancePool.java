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

package org.qi4j.runtime.composite;

/**
 * Method instance pool that keeps a linked list per thread.
 */
public final class ThreadLocalCompositeMethodInstancePool
    implements CompositeMethodInstancePool
{
    private ThreadLocal<CompositeMethodInstance> first = new ThreadLocal<CompositeMethodInstance>();

    public CompositeMethodInstance getInstance()
    {
        CompositeMethodInstance instance = first.get();
        if( instance != null )
        {
            first.set( instance.getNext() );
        }
        return instance;
    }

    public void returnInstance( CompositeMethodInstance instance )
    {
        CompositeMethodInstance currentFirstInstance = first.get();
        instance.setNext( currentFirstInstance );
        first.set( instance );
    }
}