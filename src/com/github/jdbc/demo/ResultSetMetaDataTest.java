package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;
import com.github.jdbc.common.ReflectionUtils;
import com.github.jdbc.oop.EmployeeDO;

/**
 * 使用 JDBC 驱动程序处理元数据 
 * ResultSetMetaData
	why:如果只有一个结果，但不知道该结果集中有多少列，列的名字都是什么。
	what:是描述 ResultSet的数据对象，即从中可以获取到结果集中有多少列，列名是什么。。。
	how:
	①得到ResultSetMetaData对象：调用ResultSet的getMetaData()方法
	②ResultSetMetaData有哪些好用的方法：
	int getColumnCount():SQL语句中包含哪些列；结果集中包含哪些列
	String getColumnLabel(int column):获取指定的列的别名，其中索引从1开始。获取结果集中的每一列的别名
 * @author william
 *
 */
public class ResultSetMetaDataTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testResultSetMetaData(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			String sql = "select email \"email\",first_name \"firstName\",hire_date as \"hireDate\" from employees where employee_id = ?";
			System.out.println(sql);
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, 100);
			rs = preparedStatement.executeQuery();
			// 得到ResultSetMetaData对象
			ResultSetMetaData rsmd = rs.getMetaData();
			//
			Map<String,Object> values = new HashMap<String,Object>();
			while(rs.next()){
				for(int i=0;i<rsmd.getColumnCount();i++){
					String columnLabel = rsmd.getColumnLabel(i+1);
					Object columnValue = rs.getObject(columnLabel);
					values.put(columnLabel, columnValue);
				}
			}
			System.out.println(values);
			Class clazz = EmployeeDO.class;
			Object obj = clazz.newInstance();
			for(Map.Entry<String, Object> entry: values.entrySet()){
				String fieldName = entry.getKey();
				Object fieldValue = entry.getValue();
				ReflectionUtils.setFieldValue(obj, fieldName, fieldValue);
			}
			System.out.println(obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(rs, preparedStatement,connection);
		}
	}
	
	@Test
	public void testGetEntity(){
		String sql = "select email \"email\",first_name \"firstName\",hire_date as \"hireDate\" from employees where employee_id = ?";
		EmployeeDO employee = JDBCUtil.get(EmployeeDO.class, sql, 100);
		System.out.println(employee);
	}
}
