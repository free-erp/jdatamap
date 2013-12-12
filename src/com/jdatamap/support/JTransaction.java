/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author afa
 */
public class JTransaction implements ITransaction
{
    private Connection connection;
    public JTransaction(Connection connection) throws SQLException
    {
        this.connection = connection;
        this.connection.setAutoCommit(false);
    }

    public void commit()
    {
        try
        {
            this.connection.commit();
            //this.connection.setAutoCommit(true);
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }

    public void rollback()
    {
        try
        {
            this.connection.rollback();
            //this.connection.setAutoCommit(true);
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }

}
