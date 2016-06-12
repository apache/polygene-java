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
package org.apache.zest.sample.dcicargo.sample_b.infrastructure.model;

import org.apache.wicket.model.IModel;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilderFactory;

/**
 * ReadOnlyModel
 *
 * Abstract base model for Wicket model objects taking Zest objects.
 */
public abstract class ReadOnlyModel<T>
    implements IModel<T>
{
    private static final long serialVersionUID = 1L;

    static protected ZestAPI api;
    static protected ServiceFinder serviceFinder;
    static protected UnitOfWorkFactory uowf;
    static protected ValueBuilderFactory vbf;

    /**
     * This default implementation of setObject unconditionally throws an
     * UnsupportedOperationException. Since the method is final, any subclass is effectively a
     * read-only model.
     *
     * @param object The object to set into the model
     *
     * @throws UnsupportedOperationException
     */
    public final void setObject( final T object )
    {
        throw new UnsupportedOperationException( "Model " + getClass() +
                                                 " does not support setObject(Object)" );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Model:classname=[" );
        sb.append( getClass().getName() ).append( "]" );
        return sb.toString();
    }

    public static void prepareModelBaseClass( Module m,
                                              ZestAPI api
    )
    {
        uowf = m.unitOfWorkFactory();
        serviceFinder = m;
        vbf = m;
        ReadOnlyModel.api = api;
    }
}