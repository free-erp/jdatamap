/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/**
 *
 * @author afa
 */
public class DatabaseToolkit
{
    private static final int defaultConnectionCount = 1;
    private Vector<Connection> connections = new Vector<Connection>(defaultConnectionCount);
    private String url;
    private String userName;
    private String password;
    private int databaseType;
    private static final int SQLSERVER = 1;
    private static final int ORACLE = 2;
    private static final int  MYSQL = 0;


    public DatabaseToolkit(String driveName, String url, String userName, String password) throws ClassNotFoundException, SQLException
    {
        Class.forName(driveName);
        for(int i = 0; i < defaultConnectionCount; i++)
        {
            connections.add(DriverManager.getConnection(url, userName, password));
        }
    }
    
    public DatabaseToolkit(String driveName, String url)  throws ClassNotFoundException, SQLException
    {
        Class.forName(driveName);
        for(int i = 0; i < defaultConnectionCount; i++)
        {
            connections.add(DriverManager.getConnection(url));
        }
    }

    public Connection getConnection()
    {
        try
        {
            for(Connection connection:connections)
            {
//                if (connection.isValid(0))
//                {
                    return connection;
//                }
            }
            if (userName != null)
            {
                Connection conection = DriverManager.getConnection(url, userName, password);
                connections.add(conection);
                return conection;
            }
            else
            {
                Connection conection = DriverManager.getConnection(url);
                connections.add(conection);
                return conection;
            }
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }
    public ISession getSession()
    {
        return new SessionImpl(this.getConnection());
    }
}
