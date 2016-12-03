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

package org.apache.zest.metrics.codahale;

import com.codahale.metrics.Counter;
import org.apache.zest.api.metrics.MetricsCounter;

public class CodahaleCounter
    implements MetricsCounter
{
    private Counter counter;

    public CodahaleCounter( Counter counter )
    {

        this.counter = counter;
    }

    @Override
    public void increment()
    {
        counter.inc();
    }

    @Override
    public void increment( int steps )
    {
        counter.inc( steps );
    }

    @Override
    public void decrement()
    {
        counter.dec();
    }

    @Override
    public void decrement( int steps )
    {
        counter.dec( steps );
    }
}
