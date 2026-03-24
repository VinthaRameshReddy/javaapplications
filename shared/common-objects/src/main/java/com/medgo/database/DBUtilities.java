package com.medgo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Ramesh Gade
 * @Company Snapwork Technologies
 */
public class DBUtilities {
    private static Logger logger = LoggerFactory.getLogger(DBUtilities.class);
    public static DBUtilities dbConnectionObj;
    static DataSource ds = null;
    static Context ctx = null;

    /**
     * This is for for Preventing the Multiple Instance Creation .
     */
    private DBUtilities() {

    }

    /**
     * This will create the only one indtance of the Class object
     */
    public static synchronized DBUtilities dbUtilitilyObj() {
        /**
         * If the <strong>DBUtilities</strong> class object is null this condition will
         * create object and return
         */
        if (dbConnectionObj == null) {
            dbConnectionObj = new DBUtilities();
        }
        return dbConnectionObj;
    }

    /**
     * This will close the {@code Connection#close()} object if the paramater object
     * is not null
     */
    public static void closeConnecObj(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
            } catch (Throwable ex) {
            }
        }
    }

    /**
     * This will close the {@code PreparedStatement#close()} Prepared Statement
     * object if the Prepared statement object is not null
     */

    public static void closePreparedStatementObj(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException se) {
            } catch (Throwable throwable) {
            }
        }
    }

    /**
     * This will close the {@code ResultSet#close()} ResultSet object if the
     * ResultSet object is not null
     */
    public static void closeResultsetObj(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException rs) {
            } catch (Throwable throwable) {
            }
        }
    }

    /**
     * This will close the {@code Statement#close()} Statement object if the
     * Statement object is not null
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sc) {
            } catch (Throwable throwable) {
            }
        }
    }

    /**
     * Returns {@code Connection} Creating the Connection with
     * <h1>JNDI Concept</h1> but, this is varies with the Server to Server. After
     * Creating this will return the Connection Object. If there is any
     * {@code Exception} while Creating Object this method will return <strong> Null
     * </strong> object.
     */

    public static Connection getJndiConnection() {
        //	System.out.println("getJndiConnection ::");
        Connection connection = null;
        try {
            DataSource ds = (DataSource) new InitialContext().lookup("jdbc/WLAPPDS");
            connection = ds.getConnection();

        } catch (Exception e) {
            //		System.out.println("getJndiConnection exception ::");
            //	System.out.println("exception :: " + e);
        }
        return connection;

    }


    public static Connection getDBContextConnection1() {
        Connection con = null;
        try {
            //	Class.forName("oracle.jdbc.driver.OracleDriver");
            Class.forName("oracle.jdbc.driver.OracleDriver");

            /*
             * con = DriverManager.getConnection("jdbc:oracle:thin:@120.63.243.152:1521:xe",
             * "" + usrName + "", "" + pasWord + "");
             */

            //	con = DriverManager.getConnection("jdbc:oracle:thin:@120.63.243.152:1521:xe", "DEVTEST", "DEVTEST");
            //	con = DriverManager.getConnection("jdbc:oracle:thin:@182.70.118.31:1521:xe", "DEVTEST", "DEVTEST");
            // YesAIM on Amazon
            //	con = DriverManager.getConnection("jdbc:oracle:thin:@52.70.222.95:1521:orcl", "YESBANK", "LOCAL_YESBANK");

            con = DriverManager.getConnection("jdbc:oracle:thin:@52.70.222.95:1521:orcl", "HDFCSMARTACC", "HDFCSMARTACC");


            // con =
            // DriverManager.getConnection("jdbc:oracle:thin:@10.226.214.118:1522:MEAPUAT",
            // "" + usrName + "",
            // "" + pasWord + "");

        } catch (Exception e) {
            //	logger.info("exception :: " + e);
        }
        return con;
    }


}

