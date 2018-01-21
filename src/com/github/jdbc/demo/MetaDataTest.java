package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class MetaDataTest {

	/**
	 * DatabaseMetaData 是描述 数据库 的元数据对象.可以由 Connection 得到. 
	 */
	@Test
	public void testDatabaseMetaData(){
		Connection connection = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			//可以得到数据库本身的一些基本信息
			DatabaseMetaData db = connection.getMetaData();
			//1. 得到数据库的版本号
			int version = db.getDatabaseMajorVersion();
			System.out.println(version);
			//2. 得到连接到数据库的用户名
			String user = db.getUserName();
			System.out.println(user);
			//3. 得到 MySQL 中有哪些数据库
			resultSet = db.getCatalogs();
			while(resultSet.next()){
				System.out.println(resultSet.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(resultSet, null, connection);
		}
	}
	
	@Test
	public void testResultSetMetaData(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			String sql = "select employee_id \"employeeId\",last_name \"lastName\",email \"email\" from employees where employee_id = ?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setObject(1, 999);
			resultSet = preparedStatement.executeQuery();
			// 1. 得到 ResultSetMetaData 对象
			ResultSetMetaData rsmd = resultSet.getMetaData();
			// 2. 得到列的个数
			int columnCount = rsmd.getColumnCount();
			System.out.println(columnCount);
			//
			String columnName;
			String columnLabel;
			for(int i=0; i<columnCount; i++){
				// 3. 得到列名
				columnName = rsmd.getColumnName(i+1);
				// 4. 得到列的别名
				columnLabel = rsmd.getColumnLabel(i+1);
				System.out.println(columnName + "," +columnLabel);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
	}
}
