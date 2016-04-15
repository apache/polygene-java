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

package org.apache.zest.library.logging.trace;

import java.lang.reflect.InvocationHandler;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.injection.scope.This;

/**
 * The TraceAllConcern will call the traceEntry(), traceExit() and traceException() methods in the
 * Tracer instance associated with the CompositeType that the TraceAllConcern is part of.
 * <p>
 * The Trace paradigm is all about tracking the entry and exit (both normal and exceptional ones)
 * of methods.
 * </p>
 * <p>
 * The main difference to the <code>TraceConcern</code> is that this concern is not associated with
 * the <code>@Trace</code> annotation, and all methods in the interfaces will be traced, unless
 * the LogService has turned off tracing.
 * </p>
 *
 * @see TraceConcern
 */
@AppliesTo( ExcludeCompositeFilter.class )
public final class TraceAllConcern extends AbstractTraceConcern
    implements InvocationHandler
{
    public TraceAllConcern( @This Composite composite )
    {
        super( composite );
    }

    @Override
    protected boolean doTrace()
    {
        return true;
    }
}
