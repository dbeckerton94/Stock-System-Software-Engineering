import java.sql.*;

public class SQLServer extends Thread
{
	private Connection conn;
	private String server;
	private String database;
	private String user;
	private String pass;
	private final String driver = "org.mariadb.jdbc.Driver";

	public SQLServer(String db_server, String db_name, String db_user, String db_pass)
	{
		server = db_server;
		database = db_name;
		user = db_user;
		pass = db_pass;
	}

	public Boolean connect()
	{
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mariadb://" + server + "/" + database, user, pass);
			return true;
		}
		catch ( SQLException e) {
			e.printStackTrace();
		}
		catch ( ClassNotFoundException e ){
			e.printStackTrace();
			// TODO
			// Fallback to non-SQL??
		}
		return false;
	}

	public Boolean disconnect()
	{
		try {
			conn.close();
			return true;
		}
		catch( SQLException e ) {
			e.printStackTrace();
			return false;
		}
	}

	public Boolean isConnected()
	{
		try
		{
			return conn.isValid(500);
		}
		catch(SQLException e)
		{
			return false;
		}
	}

	public ResultSet sendQuery(String statement ){
		try{
			return conn.createStatement().executeQuery( statement );
		}
		catch( SQLException e ){
			e.printStackTrace();
			return null;
		}
	}

	public Boolean sendStatement(String statement ){
		try{
			conn.createStatement().execute( statement );
			return true;
		}
		catch( SQLException e ){
			e.printStackTrace();
			return false;
		}
	}
}
