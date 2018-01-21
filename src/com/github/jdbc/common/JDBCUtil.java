package com.github.jdbc.common;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @since 2018-01-01
 * @author william
 *
 */
public class JDBCUtil {

	private static DataSource dataSource = null;
	// 数据库连接池应只被初始化一次. 
	static {
//		dataSource = new ComboPooledDataSource("mysql-c3p0");
		dataSource = new ComboPooledDataSource("oracle-c3p0");
	}
	
	public static Connection getConnectionByPool(){
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	/**
	 * 获取连接的方法. 通过读取配置文件从数据库服务器获取一个连接.
	 * @return
	 */
	public static Connection getConnection() {
		// 1. 准备连接数据库的 4 个字符串.
		// 1). 创建 Properties 对象
		Properties properties = new Properties();
		Connection connection = null;
		try {
			// 2). 获取 jdbc.properties 对应的输入流
			InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
			// 3). 加载 2） 对应的输入流
			properties.load(in);
			// 4). 具体决定 user, password 等4 个字符串.
			String user = properties.getProperty("jdbcUser");
			String password = properties.getProperty("jdbcPassword");
			String jdbcUrl = properties.getProperty("jdbcUrl");
			String jdbcDriver = properties.getProperty("jdbcDriver");
			// 2. 加载数据库驱动程序(对应的 Driver 实现类中有注册驱动的静态代码块.)
			Class.forName(jdbcDriver);
			// 3. 通过 DriverManager 的 getConnection() 方法获取数据库连接.
			connection = DriverManager.getConnection(jdbcUrl, user, password);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return connection;
	}
	
	/**
	 * 关闭 Statement 和 Connection
	 * @param statement
	 * @param connection
	 */
	public static void release(Statement statement, Connection connection){
		if(statement != null){
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		
		if(connection != null){
			try {
				//数据库连接池的 Connection对象进行 close时并不是真的进行关闭, 而是把该数据库连接会归还到数据库连接池中. 
				connection.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * 关闭 ResultSet、Statement 和 Connection
	 * 关闭的顺序是: 先关闭后获取的. 即先关闭 Statement 后关闭 Connection
	 * @param resultSet
	 * @param statement
	 * @param connection
	 */
	public static void release(ResultSet resultSet, Statement statement, Connection connection){
		if(resultSet != null){
			try {
				resultSet.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		release(statement, connection);
	}
	
	/**
	 * 通用的更新的方法: 包括 INSERT、UPDATE、DELETE
	 * @param sql
	 * @return
	 */
	public static int update(String sql){
		Connection connection = null;
		Statement statement = null;
		int result = 0;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			result = statement.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return result;
	}
	
	public static void update(String sql, Object...args){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0; i < args.length; i++){
				preparedStatement.setObject(i + 1, args[i]);
			}
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			release(preparedStatement, connection);
		}
	}
	
	/**
	 * 
	 * 通用的查询方法：可以根据传入的 SQL、Class 对象返回 SQL 对应的记录的对象
	 * @param clazz: 描述对象的类型
	 * @param sql: SQL 语句。可能带占位符
	 * @param args: 填充占位符的可变参数。
	 * @return
	 */
	public static<T> T get(Class<T> clazz, String sql,Object ...args){
		T entity = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			//1.得到ResultSet对象
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i=0; i < args.length; i++){
				preparedStatement.setObject(i+1, args[i]);
			}
			rs = preparedStatement.executeQuery();
			//2.得到ResultSetMetaData对象
			ResultSetMetaData rsmd = rs.getMetaData();
			//3.创建一个Map<String,Object>对象，键：SQL查询的列的别名，值：列的值。
			Map<String,Object> values = new HashMap<String,Object>();
			//4.处理结果集，利用ResultSetMetaData填充3对应的Map对象
			if(rs.next()){
				for(int i=0;i<rsmd.getColumnCount();i++){
					String columnLabel = rsmd.getColumnLabel(i+1);
					Object columnValue = rs.getObject(i+1);
					values.put(columnLabel, columnValue);
				}
			}
			//5.若Map不为空集，利用反射创建clazz对应的对象
			if(values.size() > 0){
				entity = clazz.newInstance();
				//6.遍历Map对象，利用反射为Class对象的对应的属性赋值。
				for(Map.Entry<String, Object> entry: values.entrySet()){
					String fieldName = entry.getKey();
					Object value = entry.getValue();
					ReflectionUtils.setFieldValue(entity, fieldName, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(rs, preparedStatement, connection);
		}
		return entity;
	}
	// 处理事务的方法
	public static void beginTx(Connection connection){
		if(connection != null){
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	public static void commit(Connection connection){
		if(connection != null){
			try {
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	public static void rollback(Connection connection){
		if(connection != null){
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
}
