/*
 * Copyright 2011 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.mongodb;

import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.mongodb.MongoEntityStoreConfiguration.WriteConcern;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

public class MongoMapEntityStoreAssembler
        implements Assembler
{

    public static final String DEFAULT_DATABASE_NAME = "qi4j:entitystore:data";
    public static final String DEFAULT_COLLECTION_NAME = "qi4j:entitystore:entities";
    private Visibility visibility = Visibility.application;
    private ModuleAssembly configModule;
    private Visibility configVisibility = Visibility.layer;
    private String hostname = "127.0.0.1";
    private Integer port = 27017;
    private String database = DEFAULT_DATABASE_NAME;
    private String collection = DEFAULT_COLLECTION_NAME;
    private WriteConcern writeConcern = WriteConcern.NORMAL;
    private List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();

    public MongoMapEntityStoreAssembler withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public MongoMapEntityStoreAssembler withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return this;
    }

    public MongoMapEntityStoreAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    /**
     * Add a MongoDB node's hostname and port.
     * 
     * Calling this method once disable the default behavior that use the MongoDB defaults: 127.0.0.1 27017
     */
    public MongoMapEntityStoreAssembler addHostnameAndPort( String hostname, Integer port )
            throws UnknownHostException
    {
        this.hostname = null;
        this.port = null;
        serverAddresses.add( new ServerAddress( hostname, port ) );
        return this;
    }

    public MongoMapEntityStoreAssembler withDatabase( String database )
    {
        this.database = database;
        return this;
    }

    public MongoMapEntityStoreAssembler withCollection( String collection )
    {
        this.collection = collection;
        return this;
    }

    public MongoMapEntityStoreAssembler withWriteConcern( WriteConcern writeConcern )
    {
        this.writeConcern = writeConcern;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        if ( configModule == null ) {
            configModule = module;
        }
        onAssemble( module, visibility, configModule, configVisibility );
    }

    private void onAssemble( ModuleAssembly module, Visibility visibility, ModuleAssembly configModule, Visibility configVisibility )
    {
        module.services( MongoMapEntityStoreService.class ).visibleIn( visibility );
        module.services( UuidIdentityGeneratorService.class );

        configModule.entities( MongoEntityStoreConfiguration.class ).visibleIn( configVisibility );
        MongoEntityStoreConfiguration mongoConfig = configModule.forMixin( MongoEntityStoreConfiguration.class ).declareDefaults();
        mongoConfig.hostname().set( hostname );
        mongoConfig.port().set( port );
        mongoConfig.database().set( database );
        mongoConfig.collection().set( collection );
        mongoConfig.writeConcern().set( writeConcern );
        mongoConfig.nodes().set( serverAddresses );
    }

}
