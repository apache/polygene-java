package org.apache.polygene.entitystore.cassandra;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.test.entity.CanRemoveAll;

public class EmptyCassandraTableMixin
    implements CanRemoveAll
{
    @This
    private CassandraCluster cluster;

    @Override
    public void removeAll()
    {
        Delete delete = QueryBuilder.delete().from(cluster.keyspaceName(), cluster.tableName());
        cluster.session().execute( delete );
    }
}
