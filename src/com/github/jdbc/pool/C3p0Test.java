package com.github.jdbc.pool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
/**
 * @see http://www.mchange.com/projects/c3p0/index.html
 * @author william
 *
 */
public class C3p0Test {

	/**
	 * 加入jar包:
	 * 	c3p0-0.9.5.2.jar
	 * 	mchange-commons-java-0.2.11.jar
	 */
	@Test
	public void testC3P0(){
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass("com.mysql.jdbc.Driver");
			cpds.setJdbcUrl("jdbc:mysql:///github");
			cpds.setUser("root");
			cpds.setPassword("github");
			System.out.println(cpds.getConnection());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * 1. 创建 c3p0-config.xml 文件,参考帮助文档中 Appendix B: Configuation Files 的内容
	 * 2. 创建 ComboPooledDataSource 实例:DataSource dataSource = new ComboPooledDataSource("named-config-c3p0");
	 * 3. 从 DataSource 实例中获取数据库连接.   
	 */
	@Test
	public void testC3p0WithConfigFile(){
		try {
			// Mysql
			DataSource dataSourceMysql = new ComboPooledDataSource("mysql-c3p0");
			System.out.println(dataSourceMysql.getConnection());
			ComboPooledDataSource comboPooledDataSourceMysql = (ComboPooledDataSource)dataSourceMysql;
			System.out.println(comboPooledDataSourceMysql.getMaxStatements());
			// Oracle
			DataSource dataSourceOrcl = new ComboPooledDataSource("oracle-c3p0");
			System.out.println(dataSourceOrcl.getConnection());
			ComboPooledDataSource comboPooledDataSourceOrcl = (ComboPooledDataSource)dataSourceOrcl;
			System.out.println(comboPooledDataSourceOrcl.getMaxStatements());
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		}
	}
	
	@Test
	public void testJdbcUtil(){
		Connection connection = JDBCUtil.getConnectionByPool();
		System.out.println(connection);
	}
	
}
