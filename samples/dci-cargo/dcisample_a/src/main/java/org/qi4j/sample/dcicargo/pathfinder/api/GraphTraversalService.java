/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.pathfinder.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Part of the external graph traversal API exposed by the routing team
 * and used by us (booking and tracking team).
 */
public interface GraphTraversalService extends Remote
{

    /**
     * @param originUnLocode      origin UN Locode
     * @param destinationUnLocode destination UN Locode
     *
     * @return A list of transit paths
     *
     * @throws RemoteException RMI problem
     */
    List<TransitPath> findShortestPath( String originUnLocode, String destinationUnLocode )
        throws RemoteException;
}
