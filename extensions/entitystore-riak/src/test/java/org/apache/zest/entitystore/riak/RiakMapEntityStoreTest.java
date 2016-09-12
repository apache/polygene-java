package org.apache.zest.entitystore.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.riak.assembly.RiakEntityStoreAssembler;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.junit.BeforeClass;

import static org.apache.zest.test.util.Assume.assumeConnectivity;

public class RiakMapEntityStoreTest
        extends AbstractEntityStoreTest
{
    @BeforeClass
    public static void beforeRiakProtobufMapEntityStoreTests()
    {
        assumeConnectivity( "localhost", 8087 );
    }
    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // END SNIPPET: assembly
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        // START SNIPPET: assembly
        new RiakEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
    }
    // END SNIPPET: assembly

    private RiakClient riakClient;
    private String bucketKey;

    @Override
    public void setUp()
            throws Exception
    {
        super.setUp();
        RiakMapEntityStoreService es = serviceFinder.findService( RiakMapEntityStoreService.class ).get();
        riakClient = es.riakClient();
        bucketKey = es.bucket();
    }

    @Override
    public void tearDown()
            throws Exception
    {
        // Riak don't expose bucket deletion in its API so we empty the Zest Entities bucket.
        Namespace namespace = new Namespace( bucketKey );
        ListKeys listKeys = new ListKeys.Builder( namespace ).build();
        ListKeys.Response listKeysResponse = riakClient.execute( listKeys );
        for( Location location : listKeysResponse )
        {
            DeleteValue delete = new DeleteValue.Builder( location ).build();
            riakClient.execute( delete );
        }
        super.tearDown();
    }
}
