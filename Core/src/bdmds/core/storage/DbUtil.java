package bdmds.core.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import bdmds.core.backend.U;

public class DbUtil
{
	/**
	 * Returns all the data packets associated with a given identifier.
	 *
	 * @param ident
	 *            The identifier to query the database with.
	 * @return A LinkedList of the data packets returned by the server.
	 * @throws SQLException
	 *             If the identifier doesn't exist, or there's a general SQL
	 *             error.
	 */
	public static synchronized LinkedList<DataPacket[]> getAllData(String ident) throws SQLException
	{
		LinkedList<DataPacket[]> res = new LinkedList<DataPacket[]>();
		DbUtil.datasetReadQuery.setString(1, ident);
		ResultSet rs = DbUtil.datasetReadQuery.executeQuery();

		// Iterate over blobs, reading them into a LinkedList.
		while (rs.next())
		{
			byte[] blob = (byte[]) rs.getObject(2);
			try
			{
				ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(blob));
				res.add((DataPacket[]) input.readObject());
			} catch (ClassNotFoundException | IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}

	public static Dataset getDatasetByName(String name) throws SQLException
	{
		for (Dataset dataset : DbUtil.getDatasets())
			if (dataset.getName().equals(name))
				return dataset;
		return null;
	}

	/**
	 * Returns the identifier associated with the given name used to create the
	 * data set.
	 *
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public static String getDatasetIdent(String name) throws SQLException
	{
		DbUtil.datasetReadIdentQuery.setString(1, name);
		ResultSet rs = DbUtil.datasetReadIdentQuery.executeQuery();
		rs.next();
		return rs.getString(1);
	}

	public static LinkedList<Dataset> getDatasets() throws SQLException
	{
		// DbUtil.datasetReadAll.setString(1, name);
		ResultSet rs = DbUtil.datasetReadAll.executeQuery();
		LinkedList<Dataset> res = new LinkedList<Dataset>();
		while (rs.next())
			res.add(new Dataset(rs.getString("name"), rs.getString("starttime"), rs.getString("identifier"), rs.getInt("datacount")));
		return res;
	}

	/**
	 * Saves a set of rows contained in the supplied DataPacket array.
	 *
	 * @param datasetIdent
	 * @param startrow
	 * @param endrow
	 * @param blob
	 * @throws SQLException
	 */
	public static <T> void saveRows(String datasetIdent, int startrow, int endrow, DataPacket[] blob) throws SQLException
	{
		U.d("Uploading blob with rows " + startrow + " -> " + endrow + " to dataset " + datasetIdent + ".", 1);

		DbUtil.dataInsertQuery.setString(1, datasetIdent);
		DbUtil.dataInsertQuery.setInt(2, startrow);
		DbUtil.dataInsertQuery.setInt(3, endrow);
		DbUtil.dataInsertQuery.setInt(4, endrow - startrow);
		DbUtil.dataInsertQuery.setObject(5, blob);
		DbUtil.dataInsertQuery.executeUpdate();
	}

	public static void setupDataset(String name, String ident) throws SQLException
	{
		String startTime = DateFormat.getInstance().format(new Date());

		DbUtil.datasetInsertQuery.setString(1, name);
		DbUtil.datasetInsertQuery.setString(2, startTime);
		DbUtil.datasetInsertQuery.setString(3, ident);
		DbUtil.datasetInsertQuery.executeUpdate();
	}

	public static void updateDataset(int counter, String ident) throws SQLException
	{
		DbUtil.datasetUpdateQuery.setInt(1, counter);
		DbUtil.datasetUpdateQuery.setString(2, ident);
		DbUtil.datasetUpdateQuery.executeUpdate();
	}

	private static final String			SQL_DATA_INSERT		= "INSERT INTO bdmds.data(datasetident, startrow, endrow, rowcount, data) VALUES (?, ?, ?, ?, ?)";
	private static final String			SQL_DATASET_INSERT	= "INSERT INTO bdmds.datasets(name, starttime, identifier) VALUES (?, ?, ?)";
	private static final String			SQL_DATASET_UPDATE	= "UPDATE bdmds.datasets SET datacount = ? WHERE identifier = ?";
	private static final String			SQL_READ_DATASET	= "SELECT startrow,data FROM bdmds.data WHERE datasetident = ? ORDER BY startrow";
	private static final String			SQL_READ_IDENT		= "SELECT identifier FROM bdmds.datasets WHERE name = ?";
	private static final String			SQL_READ_DATASETS	= "SELECT name, starttime, identifier, datacount FROM bdmds.datasets WHERE datacount > 0";

	private static PreparedStatement	dataInsertQuery;
	private static PreparedStatement	datasetInsertQuery;
	private static PreparedStatement	datasetUpdateQuery;
	private static PreparedStatement	datasetReadQuery;
	private static PreparedStatement	datasetReadIdentQuery;
	private static PreparedStatement	datasetReadAll		= null;

	private static final String			DB_URL				= "jdbc:mysql://pastlg.hopto.org:3306/bdmds";
	private static final String			DB_USERNAME			= "bdmds";
	private static final String			DB_PASSWORD			= "ogher9g083j450982h5gn4jt9io7uh45n39ohbg84j5img";

	private static Connection			db;

	static
	{
		attemptDbConnection();
		try
		{
			DbUtil.dataInsertQuery = DbUtil.db.prepareStatement(DbUtil.SQL_DATA_INSERT);
			DbUtil.datasetInsertQuery = DbUtil.db.prepareStatement(DbUtil.SQL_DATASET_INSERT);
			DbUtil.datasetUpdateQuery = DbUtil.db.prepareStatement(DbUtil.SQL_DATASET_UPDATE);
			DbUtil.datasetReadQuery = DbUtil.db.prepareStatement(DbUtil.SQL_READ_DATASET);
			DbUtil.datasetReadIdentQuery = DbUtil.db.prepareStatement(DbUtil.SQL_READ_IDENT);
			DbUtil.datasetReadAll = DbUtil.db.prepareStatement(DbUtil.SQL_READ_DATASETS);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private static void attemptDbConnection()
	{
		try
		{
			U.p("Initializing Database Connection, connecting to " + DbUtil.DB_URL);
			Class.forName("com.mysql.jdbc.Driver");
			DriverManager.setLoginTimeout(5);
			DbUtil.db = DriverManager.getConnection(DbUtil.DB_URL, DbUtil.DB_USERNAME, DbUtil.DB_PASSWORD);
		} catch (SQLTimeoutException | CommunicationsException e)
		{
			U.e("Unable to connect to primary server, falling back to local HyperSQL implementation.");
			try
			{
				Class.forName("org.hsqldb.jdbc.JDBCDriver");
				db = DriverManager.getConnection("jdbc:hsqldb:file:lcldb", DbUtil.DB_USERNAME, "");
			} catch (SQLException e1)
			{
				e1.printStackTrace();
			} catch (ClassNotFoundException e1)
			{
				e1.printStackTrace();
			}
		} catch (ClassNotFoundException | SQLException e1)
		{
			e1.printStackTrace();
		}
	}
}
