package com.github.jdbc.demo;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class StatementTest {

	/**
	 * Driver 是一个接口: 数据库厂商必须提供实现的接口. 能从其中获取数据库连接.
	 * 可以通过 Driver 的实现类对象获取数据库连接.
	 * 
	 * 1. 加入 mysql 驱动
	 * 1). 解压 mysql-connector-java-5.1.45.zip
	 * 2). 在当前项目下新建 lib 目录
	 * 3). 把 mysql-connector-java-5.1.45-bin.jar 复制到 lib 目录下
	 * 4). 右键 build-path , add to buildpath 加入到类路径下.
	 */
	@Test
	public void testDriver(){
		//1. 创建一个 Driver 实现类的对象
		Driver driver;
		try {
			driver = new com.mysql.jdbc.Driver();
			//2. 准备连接数据库的基本信息: url, user, password
			String url = "jdbc:mysql://127.0.0.1:3306/test";
			Properties info = new Properties();
			info.put("user", "root");
			info.put("password", "github");
			//3. 调用 Driver 接口的　connect(url, info) 获取数据库连接
			Connection connection = driver.connect(url, info);
			System.out.println(connection);
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * 编写一个通用的方法, 在不修改源程序的情况下, 可以获取任何数据库的连接
	 * 解决方案: 
	 * 把数据库驱动 Driver 实现类的全类名、url、user、password 放入一个配置文件中, 通过修改配置文件的方式实现和具体的数据库解耦. 
	 * oracle\product\11.2.0\dbhome_1\jdbc\lib\ojdbc6.jar
	 * @return
	 */
	public Connection getConnection(){
		Connection connection = null;
		
		String driverClass = null;
		String jdbcUrl = null;
		String jdbcUser = null;
		String jdbcPasswd = null;
		// 读取类路径下的 jdbc.properties 文件  src/jdbc.properties
		InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
		
		Properties properties = new Properties();
		try {
			properties.load(in);
			driverClass = properties.getProperty("jdbcDriver");
			jdbcUrl = properties.getProperty("jdbcUrl");
			jdbcUser = properties.getProperty("jdbcUser");
			jdbcPasswd = properties.getProperty("jdbcPassword");
			// 通过反射常见 Driver 对象. 
			Driver driver = (Driver) Class.forName(driverClass).newInstance();
			Properties info = new Properties();
			info.put("user", jdbcUser);
			info.put("password", jdbcPasswd);
			// 通过 Driver 的 connect 方法获取数据库连接. 
			connection = driver.connect(jdbcUrl, info);
		} catch (Exception e) {
			System.err.println(e);
		}
		return connection;
	}
	
	@Test
	public void testGetConnection(){
		System.out.println(getConnection());
	}
	
	/**
	 * 
	 * DriverManager 是驱动的管理类. 
	 * 1). 可以通过重载的 getConnection() 方法获取数据库连接. 较为方便
	 * 2). 可以同时管理多个驱动程序: 若注册了多个数据库连接, 则调用 getConnection()
	 * 方法时传入的参数不同, 即返回不同的数据库连接。
	 */
	@Test
	public void testDriverManager(){
		//1. 准备连接数据库的 4 个字符串. 
		//驱动的全类名.
		String driverClass = "com.mysql.jdbc.Driver";
		//JDBC URL
		String jdbcUrl = "jdbc:mysql:///test";
		//user
		String user = "root";
		//password
		String password = "github";
		Connection connection = null;		
		try {
			//2. 加载数据库驱动程序(对应的 Driver 实现类中有注册驱动的静态代码块.)
			Class.forName(driverClass);
			//3. 通过 DriverManager 的 getConnection() 方法获取数据库连接. 
			connection = DriverManager.getConnection(jdbcUrl, user, password);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		System.out.println(connection); 
	}
	
	@Test
	public void testDriverManager2(){
		System.out.println(getConnetion2());
	}
	public Connection getConnetion2(){
        Connection connection = null;
        //1. 准备连接数据库的 4 个字符串. 
		String driverClass = null;
		String jdbcUrl = null;
		String jdbcUser = null;
		String jdbcPasswd = null;
		//1). 创建 Properties 对象
		Properties properties = new Properties();
		//2). 获取 jdbc.properties 对应的输入流
		// 读取类路径下的 jdbc.properties 文件  src/jdbc.properties
		InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
		
		try {
			//3). 加载 2） 对应的输入流
			properties.load(in);
			//4). 具体决定 user, password 等4 个字符串. 
			driverClass = properties.getProperty("jdbcDriver");
			jdbcUrl = properties.getProperty("jdbcUrl");
			jdbcUser = properties.getProperty("jdbcUser");
			jdbcPasswd = properties.getProperty("jdbcPassword");
			//2. 加载数据库驱动程序(对应的 Driver 实现类中有注册驱动的静态代码块.)
			Class.forName(driverClass);
			// 通过 Driver 的 connect 方法获取数据库连接. 
			connection = DriverManager.getConnection(jdbcUrl,jdbcUser,jdbcPasswd);
		} catch (Exception e) {
			System.err.println(e);
		}
		return connection;
	}
	
	/**
	 * 
	 * 通过 JDBC 向指定的数据表中插入一条记录. 
	 * 
	 * 1. Statement: 用于执行 SQL 语句的对象
	 *    1). 通过 Connection 的 createStatement() 方法来获取
	 *    2). 通过 executeUpdate(sql) 可以执行 SQL 语句.
	 *    3). 传入的 SQL 可以是 INSRET, UPDATE 或 DELETE. 但不能是 SELECT
	 * 
	 * 2. Connection、Statement 都是应用程序和数据库服务器的连接资源. 使用后一定要关闭.
	 *    需要在 finally 中关闭 Connection 和 Statement 对象. 
	 * 
	 * 3. 关闭的顺序是: 先关闭后获取的. 即先关闭 Statement 后关闭 Connection
	 */
	@Test
	public void testStatement() {
		Connection connection = null;
		Statement statement = null;
		try {
			// 获取数据库连接
			connection = JDBCUtil.getConnection();
			// 准备插入的 SQL 语句
			String sql = null;
//			sql = "insert into employees(employee_id,last_name,email,hire_date,job_id) values (99,'william','william@oracle.com',sysdate,'IT_PROG')";
//			sql = "update employees set first_name = 'fan' where employee_id = 99";
			sql = "delete from employees where employee_id = 99";
			System.out.println(sql);
			// 获取操作 SQL 语句的 Statement 对象: 
			// 调用 Connection 的 createStatement() 方法来获取
			statement = connection.createStatement();
			// 调用 Statement 对象的 executeUpdate(sql) 执行 SQL 语句进行插入
			int result = statement.executeUpdate(sql);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			// 关闭 Statement对象 & 连接
			JDBCUtil.release(statement, connection);
		}
	}
	
	/**
	 * 
	 * ResultSet: 结果集. 封装了使用 JDBC 进行查询的结果. 
	 * 1. 调用 Statement 对象的 executeQuery(sql) 可以得到结果集.
	 * 2. ResultSet 返回的实际上就是一张数据表. 有一个指针指向数据表的第一行的前面.
	 *    可以调用 next() 方法检测下一行是否有效. 若有效该方法返回 true, 且指针下移. 相当于Iterator 对象的 hasNext() 和 next() 方法的结合体
	 * 3. 当指针对位到一行时, 可以通过调用 getXxx(index) 或 getXxx(columnName)获取每一列的值. 例如: getInt(1), getString("name")
	 * 4. ResultSet 当然也需要进行关闭. 
	 *  
	 */
	@Test
	public void testResultSet(){
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		
		try {
			connection = JDBCUtil.getConnection();
			statement = connection.createStatement();
			
			String sql = "select * from employees where employee_id = 100";
			rs = statement.executeQuery(sql);
			System.out.println(rs);
			while(rs.next()){
				int id = rs.getInt(1);
				String lastName = rs.getString("last_name");
				String email = (String) rs.getObject("email");
				Date date = rs.getDate("hire_date");
				System.out.println(id);
				System.out.println(lastName);
				System.out.println(email);
				System.out.println(date);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(rs,statement, connection);
		}
	}
}
