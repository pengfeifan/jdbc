package com.github.jdbc.demo;

import java.sql.Date;
import java.util.List;

import org.junit.Test;

import com.github.jdbc.common.DAO;
import com.github.jdbc.oop.EmployeeDO;

public class DAOTest {

	DAO dao = new DAO();
	
	@Test
	public void testUpdate(){
		String sql = "insert into employees(employee_id,last_name,email,hire_date,job_id) values (?,?,?,?,?)";
		dao.update(sql, 999,"william","william@oracle.com",new Date(new java.util.Date().getTime()),"IT_PROG");
	}
	
	@Test
	public void testGet(){
		String sql = "select employee_id \"employeeId\",last_name \"lastName\",email \"email\" from employees where employee_id = ?";
		EmployeeDO employee = dao.get(EmployeeDO.class, sql, 999);
		System.out.println(employee);
	}
	
	@Test
	public void testGetForList(){
		String sql = "select employee_id \"employeeId\",last_name \"lastName\",email \"email\" from employees where employee_id in (?,?,?)";
		List<EmployeeDO> employees = dao.getForList(EmployeeDO.class, sql, 999,100,101);
		System.out.println(employees);
	}
	
	@Test
	public void testGetForValue(){
		String sql = "select count(1) from employees";
		String count = dao.getValue(sql).toString();
		System.out.println(count);
	}
}
