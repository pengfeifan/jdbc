package com.github.jdbc.pool;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.junit.Test;

/**
 * @see http://commons.apache.org/proper/commons-dbcp/
 * @author william
 *
 */
public class DbcpTest {

	/**
	 * 使用 DBCP 数据库连接池
	 * 1. 加入 jar 包(2 个jar 包). 
	 * 	commons-dbcp2-2.2.0.jar
	 * 	commons-pool2-2.5.0.jar
	 * 2. 创建数据库连接池
	 * 3. 为数据源实例指定必须的属性
	 * 4. 从数据源中获取数据库连接
	 */
	@SuppressWarnings("resource")
	@Test
	public void testDBCP(){
		BasicDataSource dataSource = null;
		Connection connection = null;
		try {
			// 1.创建DBPC数据源实例
			dataSource = new BasicDataSource();
			// 2. 为数据源实例指定必须的属性
			dataSource.setUsername("root");
			dataSource.setPassword("github");
			dataSource.setUrl("jdbc:mysql://localhost:3306/github");
			dataSource.setDriverClassName("com.mysql.jdbc.Driver");
			// 3. 指定数据源的一些可选的属性.
			// 1). 指定数据库连接池中初始化连接数的个数
			dataSource.setInitialSize(10);
			// 2). 指定最大的连接数: 同一时刻可以同时向数据库申请的连接数
			dataSource.setMaxTotal(50);
			// 3). 指定小连接数: 在数据库连接池中保存的最少的空闲连接的数量 
			dataSource.setMinIdle(5);
			// 4).等待数据库连接池分配连接的最长时间. 单位为毫秒. 超出该时间将抛出异常.
			dataSource.setMaxWaitMillis(1000*5);
			// 4. 从数据源中获取数据库连接
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
		System.out.println(connection.getClass());
	}
	
	@SuppressWarnings("resource")
	@Test
	public void testDBCP2() throws Exception{
		//1.创建DBPC数据源实例
		final BasicDataSource dataSource=new BasicDataSource();
		//2. 为数据源实例指定必须的属性
		dataSource.setUsername("root");
		dataSource.setPassword("github");
		dataSource.setUrl("jdbc:mysql://localhost:3306/github");
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		//3. 指定数据源的一些可选的属性.
		//1). 指定数据库连接池中初始化连接数的个数
		dataSource.setInitialSize(5);
		//2). 指定最大的连接数: 同一时刻可以同时向数据库申请的连接数
		dataSource.setMaxTotal(5);
		//3). 指定小连接数: 在数据库连接池中保存的最少的空闲连接的数量 
		dataSource.setMinIdle(2);
		//4).等待数据库连接池分配连接的最长时间. 单位为毫秒. 超出该时间将抛出异常.
		dataSource.setMaxWaitMillis(1000*5);
		
		//4. 从数据源中获取数据库连接
		Connection connection=dataSource.getConnection();
		System.out.println("1." + connection);
		connection=dataSource.getConnection();
		System.out.println("2." + connection);
		connection=dataSource.getConnection();
		System.out.println("3." + connection);
		connection=dataSource.getConnection();
		System.out.println("4." + connection);
		Connection connection2 = dataSource.getConnection();
		System.out.println("5.connection2>" + connection2);
		new Thread(){
			public void run(){
				Connection connection;
				try {
					connection = dataSource.getConnection();
					System.out.println("new Thread:" + connection);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			};
		}.start();
		Thread.sleep(3000);//超出MaxWaitMillis将抛出异常
		connection2.close();
		/**
		1.1635546341, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		2.1740035246, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		3.913190639, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		4.1018937824, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		connection2>1915058446, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		new Thread:1838844114, URL=jdbc:mysql://localhost:3306/github, UserName=root@localhost, MySQL Connector Java
		 */
	}
	
	/**
	 *  1. 加载 dbcp 的 properties 配置文件: 配置文件中的键需要来自 BasicDataSource的属性.
	 *  2. 调用 BasicDataSourceFactory 的 createDataSource 方法创建 DataSource 实例
	 *  3. 从 DataSource 实例中获取数据库连接. 
	 */
	@Test
	public  void testDBCPWithDataSourceFactory() {
		Properties properties = new Properties();
		InputStream inStream = this.getClass().getClassLoader().getResourceAsStream("dbcp.properties");
		DataSource dataSource = null;
		try {
			properties.load(inStream);
			dataSource = BasicDataSourceFactory.createDataSource(properties);
			System.out.println(dataSource.getConnection());
			BasicDataSource basicDataSource = (BasicDataSource)dataSource;
			System.out.println(basicDataSource.getMaxTotal());
			System.out.println(basicDataSource.getMaxWaitMillis());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
