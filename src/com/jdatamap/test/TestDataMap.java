/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.test;

import com.jdatamap.support.DatabaseToolkit;
import com.jdatamap.support.ISession;

/**
 *
 * @author afa
 */

public class TestDataMap {
    public static void main(String args[])
    {
        String url = "jdbc:derby:c:/derbydb/money";
         String driver = "org.apache.derby.jdbc.EmbeddedDriver";
         try
         {
//            DatabaseToolkit kit = new DatabaseToolkit(driver, url);
//            ISession session = kit.getSession();
//            for(int i = 0; i < 10; i++)
//            {
//                CompanyPo po = new CompanyPo();
//                po.setCompanyName("测试公司");
//                session.insertEntity(po);
//                UserPo p = new UserPo();
//                p.setUserName("用户api");
//                p.setCompany(po);
//                session.insertEntity(p);
//            }
//
//            List<UserPo> pos = (List<UserPo>)session.findEntities(UserPo.class, "select * from UserPo", null);
//            for(UserPo po:pos)
//            {
//                System.out.println("value:" + po.getCompany().getId());
//                System.out.println("companyName:" + po.getCompany().getCompanyName());
//            }
//
//            session.close();
         }
         catch(Exception ex)
         {
             ex.printStackTrace();
         }
    }

}

/**
 1.Oracle数据库

Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();

String url = "jdbc:oracle:thin:@localhost:1521:orcle";

String user = "test";

String password = "test";

Connection conn = DriverManager.getConnection(url, user, password);


2.DB2数据库

Class.forName("com.ibm.db2.jdbc.app.DB2Driver").newInstance();

String url = "jdbc:db2://localhost:5000/testDB";

String user = "admin";

String password = "test";

Connection conn = DriverManager.getConnection(url, user, password);

3.SQL Server数据库

Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();

String url = "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=testDB";

String user = "sa";

String password = "test";

Connection conn = DriverManager.getConnection(url, user, password);

4. Sybase数据库

Class.forName("com.sybase.jdbc.SybDriver").newInstance();

String url = "jdbc:sybase:Tds:localhost:5007/testDB";

Properties sysProps = System.getProperties();

sysProps.put("user", "userid");

sysProps.put("password", "user_password");

Connection conn = DriverManager.getConnection(url, sysProps);

5.Informix数据库

Class.forName("com.infoxmix.jdbc.IfxDriver").newInstance();

String url = "jdbc:infoxmix-sqli://localhost:1533/testDB:INFORMIXSERVER=myserver;user=testuser;password=testpassword";

Connection conn = DriverManager.getConnection(url);

6.MySQL数据库

Class.forName("org.gjt.mm.mysql.Driver").newInstance();

String url = "jdbc:mysql://localhost/testDB?user=testuser&password=testpassword&useUnicode=true&characterEncoding=GB2312";

Connection conn = DriverManager.getConnection(url);

7.PostgreSQL数据库

Class.forName("org.postgresql.Driver").newInstance();

String url = "jdbc:postgresql://localhost/testDB";

String user = "myuser";

String password = "test";

Connection conn = DriverManager.getConnection(url, user, password);
 */
