package com.mobigen.iris.jdbc;

/**
 * IRISStatement.java
 * 
 *
 * HISTORY
 *    코드 변경 이력 정보를 남긴다.
 *    - 2009-05-12 : IRISStatement class 생성
 *
 * @author  sungwon Park
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.mobigen.iris.jdbc.common.io.IRISReader;
import com.mobigen.iris.jdbc.common.io.JsonIRISReader;

public class IRISStatement implements Statement {
	private boolean resultSetFetched;
	private ResultSet resultSet;

	IRISConnection irisConn = null;
	IRISSocket irisSock = null;
	private int sock_timeout;
	protected String response = null;
	private byte[] field_sep = null;
	private byte[] record_sep = null;
	private String executeQuery;
	protected boolean isLogging = false;

	protected ArrayList<String> queryList = new ArrayList<String>();
	private static IRISReader reader = new JsonIRISReader();


	IRISStatement() throws SQLException {
	}

	IRISStatement(IRISConnection irisConn) throws SQLException {

		this.irisConn = irisConn;
		this.irisSock = irisConn.irisSock;
		this.sock_timeout = this.getQueryTimeout();

		this.field_sep = Default.field_sep;
		this.record_sep = Default.record_sep;
		
		this.resultSetFetched = true;

		String sendMsg;
		String recvMsg;

		sendMsg = String.format("SET_FIELD_SEP %s\r\n", Base64.encodeBytes(this.field_sep));
		irisSock.write(sendMsg);
		recvMsg = irisSock.readline();
		sendMsg = String.format("SET_RECORD_SEP %s\r\n", Base64.encodeBytes(this.record_sep));
		irisSock.write(sendMsg);
		irisSock.readline();
	}


	public boolean SetFieldSep(String sep) throws SQLException {
		String sendMsg = String.format("SET_FIELD_SEP %s\r\n", Base64.encodeBytes(sep.getBytes()));
		this.irisSock.write(sendMsg);

		String response = this.irisSock.readline();
		if (response == null || response.charAt(0) == '-') {
			return false;
		}

		this.field_sep = sep.getBytes();
		return true;
	}

	public boolean SetFieldSep(byte[] sep) throws SQLException {
		String sendMsg = String.format("SET_FIELD_SEP %s\r\n", Base64.encodeBytes(sep));
		this.irisSock.write(sendMsg);

		String response = this.irisSock.readline();
		if (response == null || response.charAt(0) == '-') {
			return false;
		}

		this.field_sep = sep;
		return true;
	}

	public boolean SetRecordSep(String sep) throws SQLException {
		String sendMsg = String.format("SET_RECORD_SEP %s\r\n", Base64.encodeBytes(sep.getBytes()));
		this.irisSock.write(sendMsg);

		String response = this.irisSock.readline();
		if (response == null || response.charAt(0) == '-') {
			return false;
		}

		this.record_sep = sep.getBytes();
		return true;
	}

	public boolean SetRecordSep(byte[] sep) throws SQLException {
		String sendMsg = String.format("SET_RECORD_SEP %s\r\n", Base64.encodeBytes(sep));
		this.irisSock.write(sendMsg);

		String response = this.irisSock.readline();
		if (response == null || response.charAt(0) == '-') {
			return false;
		}

		this.record_sep = sep;
		return true;
	}

	public void GetFile(String bfile, String local_file_path) throws SQLException
	{
		String sendCMD = String.format("GETFILE %s\r\n", bfile);

		this.irisSock.write(sendCMD);

		try 
		{
			String retMessage = this.irisSock.readline() ;
			long   filesize = Long.parseLong( retMessage ) ;
			long   remained = filesize;

			OutputStream out = new FileOutputStream(local_file_path);

			byte[] buffer = new byte[1024];
			int		len= 0;
			int readsize = 1024;

			while ( remained > 0 )
			{
				//data = this.read(1024);
				if ( remained < 1024 ) { readsize = (int)remained; }
				len = this.irisSock.readByte(buffer, readsize);

				remained = remained - len ;

				if ( len > 0) out.write(buffer, 0, len);
			}

			out.flush();
			out.close();
			//this.rename(new File(local_file_path+".temp"), new File(local_file_path));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private boolean rename(File fromFile, File toFile) throws IOException {
		if (fromFile.isDirectory()) {
			File[] files = fromFile.listFiles();
			if (files == null) {
				//디렉토리 내 아무것도 없는 경우
				return fromFile.renameTo(toFile);
			} else {
				//디렉토리내 파일 또는 디렉토리가 존재하는 경우
				if(!toFile.mkdirs()) {
					return false;
				}
				for (File eachFile : files) {
					File toFileChild = new File(toFile, eachFile.getName());
					if (eachFile.isDirectory()) {
						if(!rename(eachFile, toFileChild)) {
							return false;
						}
					} else {
						if(!eachFile.renameTo(toFileChild)) {
							return false;
						}
					}
				}
				return fromFile.delete();
			}
		} else {
			//파일인 경우
			if(fromFile.getParent() != null) {
				if(!toFile.mkdirs()) {
					return false;
				}
			}
			return fromFile.renameTo(toFile);
		}
	}

	private void LoadCore(String table, String key, String partition, String control_file_path, String data_file_path,
						  String command) throws SQLException{

		LoadProperty loadProperty = new LoadProperty();
		this.LoadCore(table, key, partition, control_file_path, data_file_path, command, loadProperty);
	}

	private void LoadCore(String table, String key, String partition, String control_file_path, String data_file_path,
						  String command, LoadProperty loadProperty) throws SQLException{

		FileInputStream ctl_input_stream = null;
		FileInputStream data_input_stream = null;
		long ctl_size = 0;
		long data_size = 0;

		if (control_file_path != null){
			File control_file = new File(control_file_path);
			ctl_size = control_file.length();
			try {
				ctl_input_stream = new FileInputStream(control_file);
			} catch (FileNotFoundException e) {
				throw new SQLException(e);
			}
		}

		File data_file = new File(data_file_path);
		data_size = data_file.length();
		try {
			data_input_stream = new FileInputStream(data_file);
		} catch (FileNotFoundException e) {
			throw new SQLException(e);
		}

		this.LoadStream(table, key, partition, ctl_input_stream, ctl_size, data_input_stream, data_size, command, loadProperty);
	}


	private void LoadStream(String table, String key, String partition,
			InputStream control_input_stream, long ctl_size,
			InputStream data_input_stream, long data_size,
			String command, LoadProperty loadProperty) throws SQLException {

		if (!loadProperty.isCsvData() && control_input_stream == null){
			String msg = "-ERR ctl file doesn't exist.\r\n";
			parseResponse(msg);
			return;
		}

		String record_sep = new String(this.record_sep);
		if (loadProperty.isCsvData() && !(record_sep.compareTo("\n") == 0 || record_sep.compareTo("\r") == 0) ){
			String msg = "-ERR Record Separator is invalid. [";
			for (byte b : this.record_sep){
				msg += (b + ",");
			}
			msg += "]\r\n";
			parseResponse(msg);
			return;
		}

		if (loadProperty.isCsvData() && control_input_stream == null){
			control_input_stream = new ByteArrayInputStream("NULL".getBytes());
			ctl_size = 4;
		}

		String sendMsg = null;
		sendMsg = String.format("%s %s,%s,%s,%s,%s,%s\r\n",
			command, table, key, partition, ctl_size, data_size, loadProperty.toStr()
		);


		this.irisSock.write(sendMsg);

		InputStream file_list[] = {control_input_stream, data_input_stream};
		for( int i=0 ; i < file_list.length ; i++ ) {
			InputStream fis = file_list[i];
			byte buf[] = new byte[Default.BUFFER_SIZE];
			int len = 0;

			try {
				while( true ) {
					len = fis.read(buf);
					
					if( len == -1 ) {
						break;
					}
					this.irisSock.write(buf, len);
				}
				fis.close();
			} catch (IOException e) {
				throw new SQLException(e);
			}
		}
	}

	/*
	 * load - string
	 */
	public String LoadString(String table, String key, String partition, 
			String control_string, String data_string) throws SQLException {

	    LoadProperty loadProperty = new LoadProperty();
		return this.LoadString(table, key, partition, control_string, data_string, loadProperty);
	}
	public String LoadString(String table, String key, String partition,
			String control_string, String data_string, LoadProperty loadProperty) throws SQLException {

		int ctl_size = 0;
		InputStream ctl_input_stream = null;
		
		if (!loadProperty.isCsvData() || control_string != null){
			byte[] ctl_bytes = control_string.getBytes();
			ctl_size = ctl_bytes.length;
			ctl_input_stream = new ByteArrayInputStream(ctl_bytes);
		}

		byte[] data_bytes = data_string.getBytes();
		int data_size = data_bytes.length;
		InputStream data_input_stream = new ByteArrayInputStream(data_bytes);

		String command = "IMPORT";
		this.LoadStream(table, key, partition, ctl_input_stream, ctl_size, data_input_stream, data_size, command, loadProperty);
		return this.irisSock.readline();
	}
	
	/*
	 * load
	 */
	public String Load(String table, String key, String partition, 
			String control_file_path, String data_file_path) throws SQLException {

	    LoadProperty loadProperty = new LoadProperty();
		return this.Load(table, key, partition, control_file_path, data_file_path, loadProperty);
	}

	public String Load(String table, String key, String partition,
			String control_file_path, String data_file_path, LoadProperty loadProperty) throws SQLException {
		String command = "IMPORT";
		this.LoadCore(table, key, partition, control_file_path, data_file_path, command, loadProperty);
		return this.irisSock.readline();
	}
	
	/*
	 * load - inputstream
	 */
	public String Load(String table, String key, String partition, 
			InputStream controlInputstream, InputStream dataInputstream) throws SQLException {

	    LoadProperty loadProperty = new LoadProperty();
		return Load(table, key, partition, controlInputstream, dataInputstream, loadProperty);
	}
	public String Load(String table, String key, String partition,
			InputStream controlInputstream, InputStream dataInputstream, LoadProperty loadProperty) throws SQLException {
		String command = "IMPORT";

		try {
			this.LoadStream(table, key, partition, controlInputstream, controlInputstream.available(), dataInputstream, dataInputstream.available(), command, loadProperty);
		} catch (IOException e) {
			throw new SQLException(e);
		}
		return this.irisSock.readline();
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void addBatch(String sql) throws SQLException {
		this.queryList.add(sql);
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void cancel() throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public void clearBatch() throws SQLException {
		this.queryList.clear();
	}

	public void clearWarnings() throws SQLException {
	}

	public void close() throws SQLException {
		if (!this.isClosed()) {
			this.setQueryTimeout(this.sock_timeout);
			this.response = null;

			this.irisSock = null;
			this.irisConn = null;
			
			if (this.resultSet != null)
				this.resultSet.close();
		}
	}

	public boolean execute(String sql) throws SQLException {
		this.resultSet = this.executeQuery(  sql );
		return true;
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public int[] executeBatch() throws SQLException {
		int[] ret = new int[this.queryList.size()];

		for( int i=0 ; i < this.queryList.size() ; i++ ) {
			ret[i] = executeUpdate(this.queryList.get(i));
		}
		return ret;
	}

	protected void parseResponse(String response) throws SQLException {
		if( response == null ) {
//			throw new SQLException(IRISError.SOCKET_ERROR.getDesc(),
//					null, IRISError.SOCKET_ERROR.getCode());
			throw new SQLException("IRISError Socket Error", "08xx");
		} else if( response.charAt(0) == '-') {
			this.irisConn.close();
			String[] splited = response.split(" ", 2);
			String msg = splited[1];
			if( msg.startsWith("column TABLE_NAME is not unique") ) {
				throw new SQLException(IRISError.EXIST_TABLE_ERROR.getDesc(),
						null, IRISError.EXIST_TABLE_ERROR.getCode());
			} else if( msg.startsWith("Table does not exists") ) {
				throw new SQLException(IRISError.NO_TABLE_ERROR.getDesc(),
						null, IRISError.NO_TABLE_ERROR.getCode());
			} else if( msg.startsWith("SQL Syntax error") ) {
				throw new SQLException(IRISError.SQL_SYNTAX_ERROR.getDesc(),
						null, IRISError.SQL_SYNTAX_ERROR.getCode());
			} else if( msg.indexOf("LexToken") != -1 ) {
				throw new SQLException(IRISError.SQL_SYNTAX_ERROR.getDesc() + msg.substring(msg.indexOf("[")),
						null, IRISError.SQL_SYNTAX_ERROR.getCode());
			} else if( msg.indexOf("list index out of range") != -1 ) {
				// Parser 코드에서 예상하지 못한 예외.
				throw new SQLException(IRISError.SQL_SYNTAX_ERROR.getDesc(),
						null, IRISError.SQL_SYNTAX_ERROR.getCode());
			} else if( msg.indexOf("CHECK_OBJECT") != -1 ) {
				throw new SQLException(IRISError.ACCESS_DENIED_ERROR.getDesc(),
						null, IRISError.ACCESS_DENIED_ERROR.getCode());
			} else if( msg.indexOf("Invalid user") != -1 ) {
				throw new SQLException(IRISError.ACCOUNT_ERROR.getDesc(),
						null, IRISError.ACCOUNT_ERROR.getCode());
			} else if( msg.indexOf("Too large result") != -1 ) {
				throw new SQLException(IRISError.TOO_LARGE_RESULT_ERROR.getDesc(),
						null, IRISError.TOO_LARGE_RESULT_ERROR.getCode());
			} else {
//				System.out.println("response : " + response);
				//	throw new SQLException(IRISError.UNKNOWN_ERROR.getDesc(),
				throw new SQLException( IRISError.UNKNOWN_ERROR.getDesc() + " ( " + msg +" )",
						"08xx", IRISError.UNKNOWN_ERROR.getCode());
			}
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		if (this.irisConn == null)
			throw new SQLException( "Statement is closed");

		sql = QueryGeneralization.query(sql);

		try {
			// send request
			String sendMsg = String.format("EXECUTE2 %d\r\n%s", sql.getBytes("UTF-8").length, sql);
			this.irisSock.write(sendMsg);

			// receive response
			response = this.irisSock.readline();
		} catch (UnsupportedEncodingException e) {
			throw new SQLException(IRISError.UNSUPPORTED_ENCODING_ERROR.getDesc(), 
					null, IRISError.UNSUPPORTED_ENCODING_ERROR.getCode());
		}

		// parse response message
		parseResponse(response);

		IRISResultSet rs = new IRISResultSet( this );
		this.executeQuery = sql;
		return rs;
	}

	/**
	 * Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or 
	 * an SQL statement that returns nothing, such as an SQL DDL statement.
	 * 
	 * @param     sql - an SQL INSERT, UPDATE or DELETE statement or an SQL statement that returns nothing
	 * @return    0 : Success
	 * @exception SQLException 
	 */
	public int executeUpdate(String sql) throws SQLException {
		if (this.irisConn == null)
			throw new SQLException("Statement is closed");

		try {
			// send request
			String sendMsg = String.format("EXECUTE2 %d\r\n%s", sql.getBytes("UTF-8").length, sql);
			this.irisSock.write(sendMsg);

			// receive response
			response = this.irisSock.readline();
		} catch (UnsupportedEncodingException e) {
			throw new SQLException(IRISError.UNSUPPORTED_ENCODING_ERROR.getDesc(), 
					null, IRISError.UNSUPPORTED_ENCODING_ERROR.getCode());
		}

		// parse response message
		parseResponse(response);

		return 0;
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public int executeUpdate(String arg0, int arg1) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public int executeUpdate(String arg0, int[] arg1) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public int executeUpdate(String arg0, String[] arg1) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public Connection getConnection() throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");

		return this.irisConn;
	}

	public int getFetchDirection() throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");
		return ResultSet.FETCH_FORWARD;
	}

	public int getFetchSize() throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");
		return 1;
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	public int getMaxRows() throws SQLException {
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		boolean retVal = true;

		if( this.resultSetFetched )
		{
			retVal = false;
			if (this.resultSet != null)
				this.resultSet.close();
		}

		return retVal;
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public boolean getMoreResults(int arg0) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public int getQueryTimeout() throws SQLException {
		int timeout = 0;
		try {
			timeout = this.irisConn.rawSock.getSoTimeout();
		} catch (SocketException e) {
		}
		return timeout / 1000;
	}

	public ResultSet getResultSet() throws SQLException {
		if( !this.resultSetFetched)
			this.resultSetFetched = true;

		return this.resultSet;
	}

	public int getResultSetConcurrency() throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");
		return ResultSet.CONCUR_READ_ONLY;
	}

	public int getResultSetHoldability() throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	public int getResultSetType() throws SQLException {
		if (this.irisConn == null)
			throw new SQLException("Statement is closed");
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	public int getUpdateCount() throws SQLException {
		return -1;
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public boolean isClosed() throws SQLException {
		boolean result = this.irisConn == null ? true: false;
		return result;
	}

	public boolean isPoolable() throws SQLException {
		if ( this.irisConn == null)
			throw new SQLException( "Statement is closed");

		return false;
	}

	public void closeOnCompletion() throws SQLException {

	}

	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void setCursorName(String arg0) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public void setEscapeProcessing(boolean arg0) throws SQLException {

	}

	public void setFetchDirection(int direction) throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");

		if( ResultSet.FETCH_FORWARD != direction)
			throw new SQLException( "Only fetch forward is supported");
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void setFetchSize(int rows) throws SQLException {
		  if( rows < 0)
			throw new SQLException( "Invalid fetch size: " + rows);
	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void setMaxFieldSize(int arg0) throws SQLException {
		throw new SQLException( "Method not supported");
	}

	public void setMaxRows(int arg0) throws SQLException {

	}

	/**
	 * @exception SQLException 
	 *              Method not supported
	 */
	public void setPoolable(boolean arg0) throws SQLException {
		if( this.irisConn == null)
			throw new SQLException( "Statement is closed");	
	}

	public void setQueryTimeout(int time) throws SQLException {
		int timeout = 0;
		timeout = time * 1000;

		try {
			this.irisConn.rawSock.setSoTimeout(timeout);
		} catch (SocketException e) {
		}
	}

	public boolean isWrapperFor(Class<?> paramClass) throws SQLException {
		return paramClass.isInstance(this);
	}

	public <T> T unwrap(Class<T> paramClass) throws SQLException {
		try {
			if(this.isWrapperFor(paramClass)) {
				return (T) this;
			}
			throw new SQLException("The receiver is not a wrapper and does not implement the interface");
		}
		catch (Exception e) {
			throw new SQLException("The receiver is not a wrapper and does not implement the interface");
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {

		int size = 0;
		int columnCount = 0;
		String sendMsg;
		String data;
		String[] splited;
		String[] columnNameList;
		String[] columnTypeList;

		// send request
		sendMsg = String.format("METADATA\r\n");
		this.irisSock.write(sendMsg);

		// receive response
		response = this.irisSock.readline();

		// parse response message
		parseResponse(response);
		splited = response.split(" ");
		try {
			size = Integer.parseInt( splited[1] );
		} catch(Exception e) {
			throw new SQLException(IRISError.IRIS_ERROR.getDesc(), 
					null, IRISError.IRIS_ERROR.getCode());
		}

		splited = null;
		try {
			data = this.irisSock.read(size);
			ArrayList<String[]> columnList = reader.converToMetadata(data);
			columnNameList = columnList.get(0);
			columnTypeList = columnList.get(1);
			columnCount = columnNameList.length;
		} catch (Exception e) {
			throw new SQLException(IRISError.IRIS_ERROR.getDesc(), 
					null, IRISError.IRIS_ERROR.getCode());
		}

		IRISResultSetMetaData metaData = new IRISResultSetMetaData(columnNameList, columnTypeList, columnCount, this.executeQuery);

		return metaData;
	}

	public boolean getData(List<String[]> result) throws SQLException {
		/*
		 * ReQuest
		 * CONT
		 *
		 * Return and Response
		 * false  : +CONT {data_size}\r\n{data}
		 * true : +OK SUCCSS
		 */
		if (this.irisSock.isClose) {
			throw new SQLException("sock is closed");
		}
		
		result.clear();
		String response = null;

		this.irisSock.write("CONT\r\n");
		response = this.irisSock.readline();
		
		if( response == null ) {
			throw new SQLException(IRISError.SOCKET_ERROR.getDesc(), null, IRISError.SOCKET_ERROR.getCode());
		} else if (response.equals("+OK SUCCESS")) {
			return true;
		} else if( response.charAt(0) == '-') {
			/*
			 * ResultSet.next() false후에도 계속 실해할 경우 발생하는 에러를 방지
			 */
//			if (response.equals(IRISErrorMessage.closedCursor)) {
//				return true;
//			}
			throw new SQLException(response);
		}

		String[] size = response.split(" ");
		int iSize = 0;

		try {
			iSize = Integer.parseInt(size[1]);
		} catch(Exception e) {
			result.clear();
			throw new SQLException(e);
		}

		String readStr = this.irisSock.read(iSize);
		try {
			reader.convertToList(result, readStr);
		} catch (Exception e) {
			result.clear();
			throw new SQLException(e);
		}

		return false;
	}

}

class Byte {

	private ArrayList<byte[]> buf;

	public Byte() {
		buf = new ArrayList<byte[]>();
	}

	public void add(byte[] data) {
		buf.add(data);
	}

	public void add(byte[] data, int start, int len) throws UnsupportedEncodingException {

		byte[] ret;
		int idx = 0;

		if( len == -1 ) {
			len = data.length - start;
		}
		ret = new byte[len];

		for( int i = start ; i < start + len ; i++ ) {
			ret[idx] = data[i];
			idx++;
		}

		buf.add(ret);
	}

	public void set(byte[] data) {
		none();
		buf.add(data);
	}

	public byte[] get() {

		byte[] ret, tmp;
		int size = 0, idx = 0;

		Iterator<byte[]> it = buf.iterator();
		while( it.hasNext() ) {
			tmp = it.next();
			size += tmp.length;
		}

		ret = new byte[size];

		it = buf.iterator();
		while( it.hasNext() ) {
			tmp = (byte[])it.next();
			for( int i = 0 ; i < tmp.length ; i++ ) {
				ret[idx] = tmp[i];
				idx++;
			}
		}

		return ret;
	}

	public byte[] get(int start, int len) {

		byte all[] = get();
		byte ret[];
		int idx = 0;

		if( len == -1 ) {
			len = all.length - start;
		}
		ret = new byte[len];

		for( int i = start ; i < start + len ; i++ ) {
			ret[idx] = all[i];
			idx++;
		}

		return ret;
	}

	public int indexOf(byte data) {

		int offset = -1;
		byte ret[] = get();

		for( int i = 0 ; i < ret.length ; i++ ) {
			if( ret[i] == '\n' ) {
				offset = i;
				break;
			}
		}

		return offset;
	}

	public int length() {
		byte[] tmp;
		int size = 0;

		Iterator<byte[]> it = buf.iterator();
		while( it.hasNext() ) {
			tmp = it.next();
			size += tmp.length;
		}
		return size;
	}

	public void none() {
		buf.clear();
	}
}
