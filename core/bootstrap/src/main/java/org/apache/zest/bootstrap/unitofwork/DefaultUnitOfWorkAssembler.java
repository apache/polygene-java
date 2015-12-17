/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.bootstrap.unitofwork;

import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;

@Deprecated
public class DefaultUnitOfWorkAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // Do nothing - added automatically by EventBus
        //Class factoryMixin = loadMixinClass( "org.apache.zest.runtime.unitofwork.UnitOfWorkFactoryMixin" );
        //module.services( UnitOfWorkFactory.class ).withMixins( factoryMixin );

        //Class uowMixin = loadMixinClass( "org.apache.zest.runtime.unitofwork.ModuleUnitOfWork" );
        //module.transients( UnitOfWork.class ).withMixins( uowMixin );
    }

    private Class<?> loadMixinClass( String name )
        throws AssemblyException
    {
        try
        {
            return getClass().getClassLoader().loadClass( name );
        }
        catch( ClassNotFoundException e )
        {
            throw new AssemblyException( "Default UnitOfWorkFactory mixin is not present in the system." );
        }
    }
}
