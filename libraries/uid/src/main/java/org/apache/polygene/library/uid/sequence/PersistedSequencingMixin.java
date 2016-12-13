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
package org.apache.polygene.library.uid.sequence;

import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;

public class PersistedSequencingMixin
    implements Sequencing
{
    @Structure private UnitOfWorkFactory uowf;
    @This private Configuration<Sequence> sequence;

    @Override
    public Long newSequenceValue()
        throws SequencingException
    {
        synchronized( this )
        {
            ConcurrentEntityModificationException exc = null;
            UnitOfWork uow = uowf.newUnitOfWork();
            try
            {
                for( int i = 0; i < 3; i++ )
                {
                    try
                    {
                        Property<Long> property = sequence.get().currentValue();
                        long value = property.get();
                        value = value + 1;
                        property.set( value );
                        uow.complete();
                        return value;
                    }
                    catch( ConcurrentEntityModificationException e )
                    {
                        // Ignore;
                        exc = e;
                    }
                }
                throw new SequencingException( "Unable to update sequence value.", exc );
            }
            catch( UnitOfWorkCompletionException e )
            {
                throw new SequencingException( "Unable to update sequence value.", exc );
            }
        }
    }

    @Override
    public Long currentSequenceValue()
    {
        synchronized( this )
        {
            return sequence.get().currentValue().get();
        }
    }
}
