package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

/**
 * http://commons.apache.org/proper/commons-dbutils/
 * @author william
 *
 */
public class DBUtilsTest {

	// 使用 DBUtils 进行更新操作
	/**
	 * 测试 QueryRunner 类的 update 方法
	 * 该方法可用于 INSERT, UPDATE 和 DELETE
	 */
	@Test
	public void testQueryRunner(){
		// 1. 创建 QueryRunner 的实现类
		QueryRunner queryRunner = new QueryRunner();
		// 2. 使用其 update 方法
		String sql = "delete from users where username = ?";
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			queryRunner.update(connection, sql, "nameB_33932");
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	// 使用 DBUtils 进行查询操作
	/**
	 * 1.ResultSetHandler的作用: 
	 * QueryRunner的 query方法的返回值最终取决于query方法的 ResultHandler参数的 handle方法的返回值. 
	 * 
	 * 2.BeanListHandler: 把结果集转为一个 Bean的List,并返回.
	 * Bean的类型在创建 BeanListHanlder 对象时以 Class对象的方式传入.可以适应列的别名来映射 JavaBean 的属性名: 
	 * String sql = "SELECT id, name customerName, email, birth "FROM customers WHERE id = ?";
	 
	 * BeanListHandler(Class<T> type)
	 * 
	 * 3.BeanHandler:把结果集转为一个 Bean,并返回.
	 * Bean的类型在创建 BeanHandler对象时以 Class 对象的方式传入
	 
	 * BeanHandler(Class<T> type) 
	 * 
	 * 4.MapHandler:把结果集转为一个 Map对象,并返回.
	 * 若结果集中有多条记录, 仅返回第一条记录对应的 Map对象.Map的键:列名(而非列的别名), 值:列的值
	 * 
	 * 5.MapListHandler:把结果集转为一个 Map对象的集合,并返回. 
	 * Map的键: 列名(而非列的别名), 值: 列的值
	 * 
	 * 6.ScalarHandler:可以返回指定列的一个值或返回一个统计函数的值. 
	 */
	QueryRunner queryRunner=new QueryRunner();
	
	/**
	 * 1.ResultSetHandler
	 * QueryRunner的 query方法的返回值最终取决于query方法的 ResultHandler参数的 handle方法的返回值
	 */
	@Test
	public void testQuery(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select username userName, password,created_time createTime,balance from users";
			Object obj = queryRunner.query(connection, sql, new MyResultSetHandler());
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	/**
	 * 2.BeanHandler:把结果集的第一条记录转为创建BeanHandler对象时传入的Class参数对应的对象
	 */
	@Test
	public void testBeanHandler(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select username userName, password,created_time createdTime,balance from users where username = ?";
			User user = (User) queryRunner.query(connection, sql, new BeanHandler(User.class),"Tom");
			System.out.println(user);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	/**
	 * 3.BeanListHandler:把结果集转为一个List,该List不为null,
	 * 但可能为空集合（size()方法返回0）
	 * 若SQL语句的确能查询到记录，List中存放创建BeanListHandler传入的Class对应的对象
	 */
	@Test
	public void testBeanListHandler(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select username userName, password,created_time createdTime,balance from users";
			List<User> users = (List<User>) queryRunner.query(connection, sql, new BeanListHandler(User.class));
			System.out.println(users);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	/**
	 * 4.MapHandler:返回SQL对应的第一条记录对应的Map对象，
	 * 键：SQL查询的列名，值：列的值
	 */
	@Test
	public void testMapHandler(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select username userName, password,created_time createdTime,balance from users";
			Map<String,Object> result = queryRunner.query(connection, sql, new MapHandler());
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		} 
	}
	
	/**
	 * 5.MapListHandler:将结果集转为一个Map的List
	 * Map对应的一条记录：键：SQL查询的列名，值：列的值
	 * 而MapListHandler：返回的多条记录对应的Map的集合
	 */
	@Test
	public void testMapListHandler(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select username userName, password,created_time createdTime,balance from users";
			List<Map<String,Object>> result = queryRunner.query(connection, sql, new MapListHandler());
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	/**
	 * 6.ScalarHandler:把结果集转为一个数值（可以是任意基本数据类型和字符串，Date等）返回
	 * ScalarHandler: 可以返回指定列的一个值或返回一个统计函数的值. 
	 */
	@Test
	public void testScalarHandler(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnectionByPool();
			String sql = "select balance from users where username = ?";
			Object result = queryRunner.query(connection, sql, new ScalarHandler(),"Tom");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
}

class MyResultSetHandler implements ResultSetHandler{

	@Override
	public Object handle(ResultSet rs) throws SQLException {
		System.out.println("handler...");
		List<User> users = new ArrayList<User>();
		while(rs.next()){
			String name = rs.getString(1);
			String passwd = rs.getString(2);
			Double balance = rs.getDouble(4);
			User user = new User(name, passwd, balance);
			users.add(user);
		}
		return users;
	}
	
}