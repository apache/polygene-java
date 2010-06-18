/*
 * Created on 7 juin 2010
 *
 * Licenced under the Netheos Licence, Version 1.0 (the "Licence"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at :
 *
 * http://www.netheos.net/licences/LICENCE-1.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright (c) Netheos
 */
package org.qi4j.entitystore.sql.database;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.qi4j.api.service.ServiceComposite;

public interface DatabaseService
        extends ServiceComposite
{

    void startDatabase()
            throws Exception;

    void stopDatabase()
            throws Exception;

    Connection openConnection()
            throws SQLException;

    PreparedStatement prepareGetEntityStatement( Connection connection, String identity )
            throws SQLException;

    PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
            throws SQLException;

    Reader getEntityValue( ResultSet resultSet )
            throws SQLException;

    PreparedStatement prepareInsertEntityStatement( Connection connection, String identity, String value )
            throws SQLException;

    PreparedStatement prepareUpdateEntityStatement( Connection connection, String identity, String value )
            throws SQLException;

    PreparedStatement prepareRemoveEntityStatement( Connection connection, String identity )
            throws SQLException;

}
