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
package org.apache.polygene.library.restlet.assembly;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.restlet.Command;
import org.apache.polygene.library.restlet.FormField;
import org.apache.polygene.library.restlet.PolygeneEntityRestlet;
import org.apache.polygene.library.restlet.RestForm;
import org.apache.polygene.library.restlet.RestLink;
import org.apache.polygene.library.restlet.crud.EntityList;
import org.apache.polygene.library.restlet.crud.EntityListResource;
import org.apache.polygene.library.restlet.crud.EntityRef;
import org.apache.polygene.library.restlet.crud.EntityResource;
import org.apache.polygene.library.restlet.resource.CreationResource;
import org.apache.polygene.library.restlet.resource.DefaultResourceFactoryImpl;
import org.apache.polygene.library.restlet.resource.EntryPointResource;
import org.apache.polygene.library.restlet.resource.ResourceBuilder;
import org.apache.polygene.library.restlet.serialization.FormRepresentation;
import org.apache.polygene.library.restlet.serialization.JsonRepresentation;

/** This assembler should go to a module in the layer of the {@link RestletCrudModuleAssembler}.
 *
 */
public class RestletCrudConnectivityAssembler
    implements Assembler
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( EntryPointResource.class,
                       EntityListResource.class,
                       EntityResource.class,
                       CreationResource.class )
            .visibleIn( Visibility.layer );
        module.values( Command.class,
                       FormField.class,
                       RestForm.class,
                       RestLink.class,
                       EntityList.class,
                       EntityRef.class )
            .visibleIn( Visibility.layer );
        module.objects( DefaultResourceFactoryImpl.class,
                        JsonRepresentation.class,
                        FormRepresentation.class,
                        PolygeneEntityRestlet.class )
            .visibleIn( Visibility.layer );
        module.services( ResourceBuilder.class ).visibleIn( Visibility.layer );
    }
}
