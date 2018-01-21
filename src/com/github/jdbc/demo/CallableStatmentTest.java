package com.github.jdbc.demo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class CallableStatmentTest {

	/**
	 * 如何使用 JDBC 调用存储在数据库中的函数或存储过程
	 * JDBC调用存储过程
	步骤：
	1 通过Connection对象的prepareCall()方法创建一个CallableStatement对象的实例。
	在使用Connection对象的prepareCall()方法时，需要传入一个String类型的字符串，该字符串用于指明如何调用存储过程
	{?= call <procedure-name>[(<arg1>,<arg2>, ...)]}
	{call <procedure-name>[(<arg1>,<arg2>, ...)]}
	
	2 通过CallableStatement对象的registerOutParameter()方法注册OUT参数
	3 通过CallableStatement对象的setXxx()方法设定IN或IN OUT参数
	若想将参数默认值设为Null,可以使用setNull()方法
	4 通过CallableStatement对象的execute()方法执行存储过程
	5 如果所调用的是带返回参数的存储过程，还需要通过CallableStatement对象的getXxx()方法获取其返回值
	注：通过数据字典查看存储过程或函数的定义
	
	select text from user_source where lower(name)='sum_salary';
	 */
	@Test
	public  void testCallableStatment(){
		Connection connection = null;
		CallableStatement callableStatement = null;

		try {
			connection = JDBCUtil.getConnectionByPool();
			// 1. 通过 Connection 对象的 prepareCall()
			// 方法创建一个 CallableStatement 对象的实例.
			// 在使用 Connection 对象的 preparedCall() 方法时,
			// 需要传入一个 String 类型的字符串, 该字符串用于指明如何调用存储过程.
			String sql = "{?= call sum_salary(?, ?)}";
			callableStatement = connection.prepareCall(sql);

			// 2. 通过 CallableStatement 对象的 
			//reisterOutParameter() 方法注册 OUT 参数.
			callableStatement.registerOutParameter(1, Types.NUMERIC);
			callableStatement.registerOutParameter(3, Types.NUMERIC);
			
			// 3. 通过 CallableStatement 对象的 setXxx() 方法设定 IN 或 IN OUT 参数. 若想将参数默认值设为
			// null, 可以使用 setNull() 方法.
			callableStatement.setInt(2, 80);
			
			// 4. 通过 CallableStatement 对象的 execute() 方法执行存储过程
			callableStatement.execute();
			
			// 5. 如果所调用的是带返回参数的存储过程, 
			//还需要通过 CallableStatement 对象的 getXxx() 方法获取其返回值.
			double sumSalary = callableStatement.getDouble(1);
			long empCount = callableStatement.getLong(3);
			
			System.out.println(sumSalary);
			System.out.println(empCount);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(callableStatement, connection);
		}
	}
	/**
		create or replace function sum_salary(
		  dept_id employees.department_id%type,
		  sum_emp out number)
		  return number
		  is
		  v_sum employees.salary%type;
		begin
		  select sum(salary),count(employee_id) into v_sum, sum_emp
		  from employees
		  where department_id = dept_id;
		  return v_sum;
		exception
		  when no_data_found then
		    dbms_output.put_line('no data');
		  when others then
		    dbms_output.put_line(sqlcode ||':'|| sqlerrm);
		end;
	 */
}
