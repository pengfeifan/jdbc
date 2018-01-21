package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

public class BatchTest {

	// batch Statement
	@Test
	public void testBatchWithStatement(){
		Connection connection = null;
		Statement statement = null;
		String sql = null;
		try {
			connection = JDBCUtil.getConnection();
			JDBCUtil.beginTx(connection);
			statement = connection.createStatement();
			Date date=new Date(new java.util.Date().getTime());
			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				sql = "insert into users values('nameS_" + i + "','passwd','" + date + "',100.00)";
//				System.out.println(sql);
				statement.executeUpdate(sql);
			}
			long end = System.currentTimeMillis();
			System.out.println("costTime:" + (end - begin));// 11043
			JDBCUtil.commit(connection);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			JDBCUtil.rollback(connection);
		} finally {
			JDBCUtil.release(statement, connection);
		}
	}
	
	// batch PreparedStatement
	@Test
	public void testBatchWithPreparedStatement(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String sql = null;
		try {
			connection = JDBCUtil.getConnection();
			JDBCUtil.beginTx(connection);
			sql = "insert into users values(?,?,?,?)";
			preparedStatement = connection.prepareStatement(sql);
			Date date = new Date(new java.util.Date().getTime());
			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				preparedStatement.setString(1, "nameP_" + i);
				preparedStatement.setString(2, "passwd");
				preparedStatement.setDate(3, date);
				preparedStatement.setDouble(4, 200.00);
				preparedStatement.executeUpdate();
			}
			long end = System.currentTimeMillis();
			System.out.println("costTime:" + (end - begin));// 11739
			JDBCUtil.commit(connection);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			JDBCUtil.rollback(connection);
		} finally {
			JDBCUtil.release(preparedStatement, connection);
		}
	}
	
	//
	@Test
	public void testBatch(){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		String sql = null;
		try {
			connection = JDBCUtil.getConnection();
			JDBCUtil.beginTx(connection);
			sql = "insert into users values(?,?,?,?)";
			preparedStatement = connection.prepareStatement(sql);
			Date date = new Date(new java.util.Date().getTime());
			long begin = System.currentTimeMillis();
			for(int i = 0; i < 100000; i++){
				preparedStatement.setString(1, "nameB_" + i);
				preparedStatement.setString(2, "passwd");
				preparedStatement.setDate(3, date);
				preparedStatement.setDouble(4, 200.00);
				// "积攒" SQL 
				preparedStatement.addBatch();
				// 当 "积攒" 到一定程度, 就统一的执行一次. 并且清空先前 "积攒" 的 SQL
				if((i + 1)%500 == 0){
					preparedStatement.executeBatch();
					preparedStatement.clearBatch();
				}
			}
			// 若总条数不是批量数值的整数倍, 则还需要再额外的执行一次. 
			preparedStatement.executeBatch();
			preparedStatement.clearBatch();
			long end = System.currentTimeMillis();
			System.out.println("costTime:" + (end - begin));// 12373
			JDBCUtil.commit(connection);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			JDBCUtil.rollback(connection);
		} finally {
			JDBCUtil.release(preparedStatement, connection);
		}
	}
}
