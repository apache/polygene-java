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
 */
package org.qi4j.library.struts2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class ActionConfiguration
    implements Assembler, Serializable
{
    static final long serialVersionUID = 1L;

    private final Set<Class> objectTypes;
    private final Set<Class<? extends Composite>> compositeTypes;

    public ActionConfiguration()
    {
        compositeTypes = new HashSet<Class<? extends Composite>>();
        objectTypes = new HashSet<Class>();
    }

    public void addObjects( Class... objectTypes )
        throws AssemblyException
    {
        this.objectTypes.addAll( Arrays.asList( objectTypes ) );
    }

    public void addComposites( Class<? extends Composite>... compositeTypes )
        throws AssemblyException
    {
        this.compositeTypes.addAll( Arrays.asList( compositeTypes ) );
    }

    public Set<Class> getClasses()
    {
        Set<Class> classes = new HashSet<Class>( objectTypes );
        classes.addAll( compositeTypes );
        return Collections.unmodifiableSet( classes );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( objectTypes.toArray( new Class[]{ } ) ).visibleIn( Visibility.module );
        module.transients( (Class<? extends TransientComposite>[]) compositeTypes.toArray( new Class[]{ } ) )
            .visibleIn( Visibility.module );
    }
}
