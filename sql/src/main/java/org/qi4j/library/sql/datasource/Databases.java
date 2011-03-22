package org.qi4j.library.sql.datasource;

import org.qi4j.api.io.Input;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.util.Visitor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility methods for performing SQL calls. Wraps a given DataSource.
 */
public class Databases
{
   DataSource source;

   /**
    * Create a new Databases wrapper for a given DataSource
    *
    * @param source
    */

   public Databases( DataSource source )
   {
      this.source = source;
   }

   /**
    * Perform SQL update statement
    *
    * @param sql
    * @return
    * @throws SQLException
    */
   public int update(String sql) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         try
         {
            return stmt.executeUpdate();
         } finally
         {
            stmt.close();
         }
      } finally
      {
         connection.close();
      }
   }

   /**
    * Perform SQL update statement. If the SQL string contains ? placeholders, use
    * the StatementVisitor to update the PreparedStatement with actual values.
    *
    * @param sql
    * @param visitor
    * @return
    * @throws SQLException
    */
   public int update(String sql, StatementVisitor visitor) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         try
         {
            visitor.visit( stmt );
            return stmt.executeUpdate();
         } finally
         {
            stmt.close();
         }
      } finally
      {
         connection.close();
      }
   }

   /**
    * Perform SQL query and let visitor handle results.
    *
    * @param sql
    * @param visitor
    * @throws SQLException
    */
   public void query(String sql, ResultSetVisitor visitor) throws SQLException
   {
      query(sql, null, visitor);
   }

   /**
    * Perform SQL query and let visitor handle results. If the SQL string contains ? placeholders, use
    * the StatementVisitor to update the PreparedStatement with actual values.
    *
    * @param sql
    * @param statement
    * @param resultsetVisitor
    * @throws SQLException
    */
   public void query(String sql, StatementVisitor statement, ResultSetVisitor resultsetVisitor) throws SQLException
   {
      Connection connection = source.getConnection();
      try
      {
         PreparedStatement stmt = connection.prepareStatement( sql);
         if (statement != null)
            statement.visit( stmt );
         try
         {
            ResultSet resultSet = stmt.executeQuery();
            try
            {
               while (resultSet.next())
               {
                  if (!resultsetVisitor.visit( resultSet ))
                     return;
               }
            } finally
            {
               resultSet.close();
            }
         } finally
         {
            stmt.close();
         }
      } finally
      {
         connection.close();
      }
   }

   /**
    *
    * Perform SQL query and provide results as an Input. This makes it possible to combine
    * SQL data with the I/O API.
    *
    * @param sql
    * @return
    */
   public Input<ResultSet, SQLException> query(final String sql)
   {
      return new Input<ResultSet, SQLException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ResultSet, ReceiverThrowableType> output) throws SQLException, ReceiverThrowableType
         {
            output.receiveFrom(new Sender<ResultSet, SQLException>()
            {
               @Override
               public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super ResultSet, ReceiverThrowableType> receiver) throws ReceiverThrowableType, SQLException
               {
                  query(sql, new ResultSetVisitor()
                  {
                     @Override
                     public boolean visit(ResultSet visited) throws SQLException
                     {
                        try
                        {
                           receiver.receive(visited);
                        } catch (Throwable receiverThrowableType)
                        {
                           throw new SQLException(receiverThrowableType);
                        }

                        return true;
                     }
                  });
               }
            });
         }
      };
   }

   /**
    *
    * Perform SQL query and provide results as an Input. This makes it possible to combine
    * SQL data with the I/O API. If the SQL string contains ? placeholders, use
    * the StatementVisitor to update the PreparedStatement with actual values.
    *
    * @param sql
    * @return
    */
   public Input<ResultSet, SQLException> query(final String sql, final StatementVisitor visitor)
   {
      return new Input<ResultSet, SQLException>()
      {
         @Override
         public <ReceiverThrowableType extends Throwable> void transferTo(Output<? super ResultSet, ReceiverThrowableType> output) throws SQLException, ReceiverThrowableType
         {
            output.receiveFrom(new Sender<ResultSet, SQLException>()
            {
               @Override
               public <ReceiverThrowableType extends Throwable> void sendTo(final Receiver<? super ResultSet, ReceiverThrowableType> receiver) throws ReceiverThrowableType, SQLException
               {
                  query(sql, visitor, new ResultSetVisitor()
                  {
                     @Override
                     public boolean visit(ResultSet visited) throws SQLException
                     {
                        try
                        {
                           receiver.receive(visited);
                        } catch (Throwable receiverThrowableType)
                        {
                           throw new SQLException(receiverThrowableType);
                        }

                        return true;
                     }
                  });
               }
            });
         }
      };
   }

   /**
    * Visitor for PreparedStatements. These are created when the SQL statements contain ? placeholders.
    */
   public interface StatementVisitor
   {
      void visit( PreparedStatement preparedStatement)
         throws SQLException;
   }

   /**
    * Visitor for the ResultSet. Only access data from the given ResultSet, as the iteration will be done
    * by this API.
    *
    */
   public interface ResultSetVisitor
      extends Visitor<ResultSet, SQLException>
   {

   }
}
