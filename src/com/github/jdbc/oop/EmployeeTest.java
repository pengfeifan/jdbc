package com.github.jdbc.oop;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class EmployeeTest {

	@SuppressWarnings("resource")
	public EmployeeDO getEmployeeFromConsole(){
		EmployeeDO employee = new EmployeeDO();
		Scanner scanner = new Scanner(System.in);
		// employee_id,last_name,email,hire_date,job_id
		System.out.println("employee_id");
		employee.setEmployeeId(scanner.nextInt());
		System.out.println("last_name");
		employee.setLastName(scanner.next());
		System.out.println("email");
		employee.setEmail(scanner.next());
		System.out.println("job_id");
		employee.setJobId(scanner.next());
		employee.setHireDate(new Date());
		return employee;
	}
	
	public void addNewEmp(EmployeeDO emp){
		String sql = "insert into employees(employee_id,last_name,email,hire_date,job_id) values (" + emp.getEmployeeId() 
		+ ",'" + emp.getLastName() + "','" 
		+ emp.getEmail() + "',to_date('" + new java.sql.Date(emp.getHireDate().getTime()) + "','yyyy-MM-dd'),'IT_PROG')";
		System.out.println(sql);
		JDBCUtil.update(sql);
	}
	
	@Test
	public void testAddEmp(){
		EmployeeDO emp = getEmployeeFromConsole();
		addNewEmp(emp);
	}
	
	public int getEmpIdFromConsole(){
		System.out.println("input employee id:");
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		int empId = scanner.nextInt();
		return empId;
	}
	
	public EmployeeDO getEmp(String sql){
		EmployeeDO emp = new EmployeeDO();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		
		try {
			connection = JDBCUtil.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
			
			while(rs.next()){
				emp.setEmployeeId(rs.getInt(1));
				emp.setFirstName(rs.getString("first_name"));
				emp.setLastName(rs.getString(3));
				emp.setEmail(rs.getString("email"));
				emp.setPhoneNumber(rs.getString(5));
				emp.setHireDate(rs.getDate(6));
				emp.setJobId(rs.getString(7));
				emp.setSalary(rs.getLong(8));
				emp.setCommissionPct(rs.getLong(9));
				emp.setManagerId(rs.getInt(10));
				emp.setDepartmentId(rs.getInt(11));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(rs,statement, connection);
		}
		return emp;
	}
	
	@Test
	public void testQueryEmp(){
		int empId = getEmpIdFromConsole();
		String sql = "select * from employees where employee_id = " + empId;
		EmployeeDO emp = getEmp(sql);
		System.out.println(emp);
	}
	
}
