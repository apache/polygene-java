package org.apache.polygene.entitystore.zookeeper;

import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZkUtil
{
    static void cleanUp( String host, String znodeName )
        throws KeeperException, InterruptedException, IOException
    {
        ZooKeeper zk = new ZooKeeper( host, 10000, event -> {
        } );
        delete( zk, znodeName );
    }

    private static void delete( ZooKeeper zk, String znodeName )
        throws KeeperException, InterruptedException
    {
        Stat stat = zk.exists( znodeName, false );
        if( stat != null )
        {
            List<String> children = zk.getChildren( znodeName, false );
            for( String child : children )
            {
                delete( zk, znodeName + "/" + child );
            }
            zk.delete( znodeName, stat.getVersion() );
        }
    }
}
