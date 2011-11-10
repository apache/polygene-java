/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.bootstrap;

/**
 * Base class for assembly visitors. Subclass and override
 * the particular methods you are interested in.
 */
public class AssemblyVisitorAdapter<ThrowableType extends Throwable>
    implements AssemblyVisitor<ThrowableType>
{
    public void visitApplication( ApplicationAssembly assembly )
        throws ThrowableType
    {
    }

    public void visitLayer( LayerAssembly assembly )
        throws ThrowableType
    {
    }

    public void visitModule( ModuleAssembly assembly )
        throws ThrowableType
    {
    }

    public void visitComposite( TransientDeclaration declaration )
        throws ThrowableType
    {
    }

    public void visitEntity( EntityDeclaration declaration )
        throws ThrowableType
    {
    }

    public void visitService( ServiceDeclaration declaration )
        throws ThrowableType
    {
    }

    public void visitImportedService( ImportedServiceDeclaration declaration )
        throws ThrowableType
    {
    }

    public void visitValue( ValueDeclaration declaration )
        throws ThrowableType
    {
    }

    public void visitObject( ObjectDeclaration declaration )
        throws ThrowableType
    {
    }
}
