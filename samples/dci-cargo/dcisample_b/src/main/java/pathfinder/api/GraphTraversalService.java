package pathfinder.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

/**
 * Part of the external graph traversal API exposed by the routing team
 * and used by us (booking and tracking team).
 *
 */
public interface GraphTraversalService extends Remote {

  /**
   *
   * @param departureDate
   * @param originUnLocode origin UN Locode
   * @param destinationUnLocode destination UN Locode
   * @return A list of transit paths
   * @throws RemoteException RMI problem
   */
  List<TransitPath> findShortestPath( Date departureDate, String originUnLocode, String destinationUnLocode ) throws RemoteException;
  List<TransitPath> getVoyages( ) throws RemoteException;
}
