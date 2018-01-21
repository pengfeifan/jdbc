package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class PreparedStatementTest {

	@Test
	public void testSQLInjection() {
		String username = "a'OR PASSWORD=";
		String password = " OR '1'='1";
		String sql = "select * from users where username='" + username
				+ "'and password='" + password + "';";
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			statement = connection.createStatement();
			System.out.println(sql);
			resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				System.out.println("succeed logon");
			} else {
				System.out.println("no pass");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(resultSet, statement, connection);
		}
	}
	
	@Test
	public void testSQLInjection2() {
		String username = "a'OR PASSWORD=";
		String password = " OR '1'='1";
		String sql = "select * from users where username=? and password=?;";
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			System.out.println(sql);
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				System.out.println("succeed logon");
			} else {
				System.out.println("no pass");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
	}
	/**
	 	使用PrepareStatement           why-->what-->how
		1)why?
		①Statement需要进行拼写，很辛苦，而且容易出错
		②可以有效的禁止SQL注入
			1.SQL 注入是利用某些系统没有对用户输入的数据进行充分的检查，
			而在用户输入数据中注入非法的 SQL 语句段或命令，从而利用系统的 SQL 引擎完成恶意行为的做法；
			2.对于 Java 而言，要防范 SQL 注入，只要用 PreparedStatement 取代 Statement 就可以了
		
		2)PreparedStatement:是Statement的子接口，可以传入带占位符的SQL语句，
		并且提供了补充占位符变量的方法。
		3)使用PrepareStatement
		
		①创建PrepareStatement:
		String sql="insert into examstudent values(?,?,?,?,?,?,?)";
		PrepareStatement ps=conn.prepareStatement(sql);
		
		②调用PreparedStatement的setXxx(int index,Object val)设置占位符的值,index值从1开始
		
		③执行SQL语句：executeQuery()或executeUpdate().注意：执行时不再需要传入SQL语句
	 */
	@Test
	public void testPreparedStatement(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = JDBCUtil.getConnection();
			String sql = "insert into users(username,password,created_time) values(?,?,?)";
			// ①创建PrepareStatement
			preparedStatement = connection.prepareStatement(sql);
			// ②调用PreparedStatement的setXxx(int index,Object val)设置占位符的值,index值从1开始
			preparedStatement.setString(1, "userA");
			preparedStatement.setString(2, "passwd");
			preparedStatement.setDate(3, new java.sql.Date(new Date().getTime()));
			// ③执行SQL语句：executeQuery()或executeUpdate().注意：执行时不再需要传入SQL语句
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(preparedStatement, connection);
		}
	}
}
