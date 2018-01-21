package com.github.jdbc.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import com.github.jdbc.common.DAO;
import com.github.jdbc.common.JDBCUtil;

/**
 * 数据库事务
 * 
	在数据库中,所谓[事务]是指一组逻辑操作单元,使数据从一种状态变换到另一种状态。
	为确保数据库中数据的[一致性],数据的操纵应当是离散的成组的逻辑单元:当它全部完成时,数据的一致性可以保持,而当这个单元中的一部分操作失败,整个事务应全部视为错误,所有从起始点以后的操作应全部回退到开始状态。 
	事务的操作:先定义开始一个事务,然后对数据作修改操作,这时如果提交(COMMIT),这些修改就永久地保存下来,如果回退(ROLLBACK),数据库管理系统将放弃所作的所有修改而回到开始事务时的状态。
	事务的ACID(acid)属性
	1. 原子性（Atomicity）
		原子性是指事务是一个不可分割的工作单位，事务中的操作要么都发生，要么都不发生。 
	2. 一致性（Consistency）
		事务必须使数据库从一个一致性状态变换到另外一个一致性状态。
	3. 隔离性（Isolation）
		事务的隔离性是指一个事务的执行不能被其他事务干扰，即一个事务内部的操作及使用的数据对并发的其他事务是隔离的，并发执行的各个事务之间不能互相干扰。
	4. 持久性（Durability）
		持久性是指一个事务一旦被提交，它对数据库中数据的改变就是永久性的，接下来的其他操作和数据库故障不应该对其有任何影响
		
 * 事务的隔离级别
 * 
	对于同时运行的多个事务, 当这些事务访问数据库中相同的数据时, 如果没有采取必要的隔离机制, 就会导致各种并发问题:
		脏读: 对于两个事务 T1, T2, T1 读取了已经被 T2 更新但还没有被提交的字段. 之后, 若 T2 回滚, T1读取的内容就是临时且无效的.
		不可重复读: 对于两个事务 T1, T2, T1 读取了一个字段, 然后 T2 更新了该字段. 之后, T1再次读取同一个字段, 值就不同了.
		幻读: 对于两个事务 T1, T2, T1 从一个表中读取了一个字段, 然后 T2 在该表中插入了一些新的行. 之后, 如果 T1 再次读取同一个表, 就会多出几行.
	数据库事务的隔离性: 数据库系统必须具有隔离并发运行各个事务的能力, 使它们不会相互影响, 避免各种并发问题. 
	一个事务与其他事务隔离的程度称为[隔离级别].
	数据库提供的4种事务隔离级别：
	① Read Uncommitted(读未提交数据):允许事务读取未被其他事务提交的变更。脏读、不可重复度和幻读的问题都会出现。
	② Read Committed(读已提交数据):只允许事务读取已经被其他事务提交的变更。可以避免脏读，但不可重复读和幻读问题仍然可能出现。
	③ Repeatable Read(可重复读):确保事务可以多次从一个字段中读取相同的值，在这个事务持续期间，禁止其他事务对这个字段进行更新。可以避免脏读和不可重复读，但幻读的问题仍然存在。
	④ Serializable(串行化):确保事务可以从一个表中读取相同的行，在这个事务持续期间，禁止其他事务对该表执行插入、更新和删除所有操作。所有的并发问题都可以避免，但性能十分低下。
	数据库规定了多种事务隔离级别, 不同隔离级别对应不同的干扰程度, 隔离级别越高, 数据一致性就越好, 但并发性越弱
 * Oracle 支持2种事务隔离级别:Read Committed和Serializable，默认Read Committed
 * MySQL  支持4种事务隔离级别，默认Repeatable Read
 * @author william
 *
 */
public class TransactionTest {

	/**
	 * Tom 给 Jerry 汇款 500 元.
	 * 关于事务: 1. 如果多个操作, 每个操作使用的是自己的单独的连接, 则无法保证事务. 
	 * 2. 具体步骤: 
	 * 1). 事务操作开始前, 开始事务:取消 Connection 的默认提交行为. connection.setAutoCommit(false);
	 * 2). 如果事务的操作都成功,则提交事务: connection.commit();
	 * 3). 回滚事务: 若出现异常, 则在 catch 块中回滚事务:
	 */
	@Test
	public void testTransaction(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnection();
			// 开始事务：取消默认提交
			connection.setAutoCommit(false);
			String sql = "update users set balance = balance-500 where username = 'Tom'";
			update(connection,sql);
			int i = 10/0;
			System.out.println(i);
			sql = "update users set balance = balance+500 where username = 'Jerry'";
			update(connection,sql);
			// 提交事务
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			try {
				// 回滚事务
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testWithoutTransaction(){
		DAO dao = new DAO();
		String sql = "update users set balance = balance-500 where username = 'Tom'";
		dao.update(sql);
		int i = 10/0;
		sql = "update users set balance = balance+500 where username = 'Jerry'";
		dao.update(sql);
	}
	
	public void update(Connection connection,String sql, Object... args) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			for(int i=0;i<args.length;i++){
				preparedStatement.setObject(i + 1, args[i]);
			}
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			JDBCUtil.release(null, preparedStatement, null);
		}
	}
	
	/**
	 * 测试事务的隔离级别 在 JDBC 程序中可以通过 Connection 的 setTransactionIsolation 来设置事务的隔离级别.
	 */
	@Test
	public void testTransactionIsolation(){
		Connection connection = null;
		try {
			connection = JDBCUtil.getConnection();
			connection.setAutoCommit(false);
			String sql = "update users set balance = balance-500 where username = 'Tom'";
			update(connection,sql);
			// 设置断点
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(null, connection);
		}
	}
	
	@Test
	public void testTransactionIsolationRead(){
		String sql = "select balance from users where username = 'Tom'";
		Double balance = getForValue(sql);
		System.out.println(balance);
	}
	
	// 返回某条记录的某一个字段的值 或 一个统计的值(一共有多少条记录等.)
	@SuppressWarnings("unchecked")
	public <E> E getForValue(String sql, Object... args) {
		// 1. 得到结果集: 该结果集应该只有一行, 且只有一列
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			// 1. 得到结果集
			connection = JDBCUtil.getConnection();
			
//			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			System.out.println(connection.getTransactionIsolation()); 
			preparedStatement = connection.prepareStatement(sql);

			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i + 1, args[i]);
			}

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return (E) resultSet.getObject(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
		return null;
	}
}
