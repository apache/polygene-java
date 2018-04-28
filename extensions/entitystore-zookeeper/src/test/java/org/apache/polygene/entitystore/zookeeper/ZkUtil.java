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
