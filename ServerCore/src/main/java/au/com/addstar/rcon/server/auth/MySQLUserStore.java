package au.com.addstar.rcon.server.auth;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import au.com.addstar.rcon.server.User;

public class MySQLUserStore implements IUserStore
{
	private Connection mConnection;
	private String mHost;
	private String mDatabase;
	private String mUsername;
	private String mPassword;
	
	private PreparedStatement mLoadUser;
	private PreparedStatement mSaveUser;
	private PreparedStatement mAddUser;
	private PreparedStatement mRemoveUser;
	
	public MySQLUserStore(String host, String database, String username, String password)
	{
		mHost = host;
		mDatabase = database;
		mUsername = username;
		mPassword = password;
	}
	
	@Override
	public void initialize() throws IOException
	{
		try
		{
			// Initiate the connection
			Class.forName("com.mysql.jdbc.Driver");
			mConnection = DriverManager.getConnection("jdbc:mysql://" + mHost + "/" + mDatabase, mUsername, mPassword);
			
			// Make sure the needed table is setup
			ensureTable();
			
			// Prepare the statements
			mLoadUser = mConnection.prepareStatement("SELECT `Password`,`Salt`,`Restricted` FROM `RconAccounts` WHERE `Name`=?");
			mSaveUser = mConnection.prepareStatement("UPDATE `RconAccounts` SET `Password`=?,`Salt`=?,`Restricted`=? WHERE `Name`=?");
			mAddUser = mConnection.prepareStatement("INSERT `RconAccounts` VALUES(?,?,?,?)");
			mRemoveUser = mConnection.prepareStatement("DELETE FROM `RconAccounts` WHERE `Name`=?");
		}
		catch (ClassNotFoundException e)
		{
			throw new IOException("MySQL Database driver is not available.");
		}
		catch ( SQLException e )
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public void shutdown() throws IOException
	{
		try
		{
			mConnection.close();
		}
		catch ( SQLException e )
		{
			throw new IOException(e);
		}
	}
	
	private void ensureConnection() throws IOException, SQLException
	{
		if(!mConnection.isValid(10))
			initialize();
	}
	
	private void ensureTable() throws SQLException
	{
		Statement statement = mConnection.createStatement();
		
		try
		{
			// Do a schema check
			statement.executeQuery("SELECT * FROM `RconAccounts` LIMIT 0");
			
			return; // Table exists
		}
		catch(SQLException e)
		{
			// Table does not exist
		}
		
		statement.executeUpdate("CREATE TABLE `RconAccounts` (`Name` VARCHAR(30) PRIMARY KEY, `Password` VARCHAR(128) NOT NULL, `Salt` VARCHAR(32) NOT NULL, `Restricted` TINYINT(1) DEFAULT 1 NOT NULL)");
	}

	@Override
	public boolean loadUser( User user ) throws IOException
	{
		try
		{
			ensureConnection();
			
			mLoadUser.setString(1, user.getName());
			ResultSet result = mLoadUser.executeQuery();
			
			if(result.next())
			{
				StoredPassword password = new StoredPassword(result.getString(1), result.getString(2));
				user.setPassword(password);
				user.setIsRestricted(result.getBoolean(3));
				
				result.close();
				return true;
			}
			else
			{
				result.close();
				return false;
			}
		}
		catch(SQLException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void saveUser( User user ) throws IOException
	{
		try
		{
			ensureConnection();
			
			mSaveUser.setString(1, user.getPassword().getHash());
			mSaveUser.setString(2, user.getPassword().getSalt());
			mSaveUser.setBoolean(3, user.isRestricted());
			mSaveUser.setString(4, user.getName());
			mSaveUser.executeUpdate();
		}
		catch(SQLException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void addUser( User user ) throws IOException
	{
		try
		{
			ensureConnection();
			
			mAddUser.setString(1, user.getName());
			mAddUser.setString(2, user.getPassword().getHash());
			mAddUser.setString(3, user.getPassword().getSalt());
			mAddUser.setBoolean(4, user.isRestricted());
			mAddUser.executeUpdate();
		}
		catch(SQLException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void removeUser( User user ) throws IOException
	{
		try
		{
			ensureConnection();
			
			mRemoveUser.setString(1, user.getName());
			mRemoveUser.executeUpdate();
		}
		catch(SQLException e)
		{
			throw new IOException(e);
		}
	}

}
