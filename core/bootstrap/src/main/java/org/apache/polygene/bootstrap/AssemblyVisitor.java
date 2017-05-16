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

package org.apache.polygene.bootstrap;

/**
 * Visitor interface to visit the whole or parts of an assembly.
 * <p>
 * Implement this interface and call visit() on ApplicationAssembly, LayerAssembly or ModuleAssembly.
 * </p>
 * <p>
 * This can be used to, for example, add metadata to all entities, add concerns on composites, or similar.
 * </p>
 */
public interface AssemblyVisitor<ThrowableType extends Throwable>
{
    void visitApplication( ApplicationAssembly assembly )
        throws ThrowableType;

    void visitLayer( LayerAssembly assembly )
        throws ThrowableType;

    void visitModule( ModuleAssembly assembly )
        throws ThrowableType;

    void visitComposite( TransientDeclaration declaration )
        throws ThrowableType;

    void visitEntity( EntityDeclaration declaration )
        throws ThrowableType;

    void visitService( ServiceDeclaration declaration )
        throws ThrowableType;

    void visitImportedService( ImportedServiceDeclaration declaration )
        throws ThrowableType;

    void visitValue( ValueDeclaration declaration )
        throws ThrowableType;

    void visitObject( ObjectDeclaration declaration )
        throws ThrowableType;
}
