package com.github.jdbc.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.junit.Test;

import com.github.jdbc.common.JDBCUtil;

/**
 * 
	LOB，即Large Objects（大对象），
	是用来存储大量的二进制和文本数据的一种数据类型（一个LOB字段可存储可多达4GB的数据）。
	LOB 分为两种类型：内部LOB和外部LOB。
	内部LOB将数据以【字节流】的形式存储在数据库的内部。因而，内部LOB的许多操作都可以参与事务，也可以像处理普通数据一样对其进行备份和恢复操作。
	Oracle支持三种类型的内部LOB：
	BLOB（二进制数据）  
	CLOB（单字节字符数据） 
	NCLOB（多字节字符数据）。
	CLOB和NCLOB类型适用于存储超长的文本数据，【BLOB】字段适用于存储大量的二进制数据，如图像、视频、音频，文件等。
	目前只支持一种外部LOB类型，即BFILE类型。在数据库内，该类型仅存储数据在操作系统中的位置信息，
	而数据的实体以外部文件的形式存在于操作系统的文件系统中。因而，该类型所表示的数据是只读的，不参与事务。
	该类型可帮助用户管理大量的由外部程序访问的文件。
 * @author william
 *
 */
public class BlobTest {

	@Test
	public void testGetKeyValue(){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = JDBCUtil.getConnection();
			String sql = "insert into customers(name,email,birth) values(?,?,?)";
//			ps = connection.prepareStatement(sql);
			// 使用重载的 prepareStatement(sql, flag) 来生成 PreparedStatement 对象
			ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, "name");
			ps.setString(2, "name@12.com");
			ps.setDate(3, new Date(new java.util.Date().getTime()));
			ps.executeUpdate();
			// 通过 getGeneratedKeys() 获取包含了新生成的主键的 ResultSet 对象
			rs = ps.getGeneratedKeys();
			// 在 ResultSet 中只有一列 GENERATED_KEY, 用于存放新生成的主键值.
			if(rs.next()){
				System.out.println(rs.getObject(1));
			}
			ResultSetMetaData rsmd = rs.getMetaData();
			for(int i=0; i<rsmd.getColumnCount(); i++){
				System.out.println(rsmd.getColumnName(i+1));
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(rs, ps, connection);
		}
	}
	
	/**
	 * 插入 BLOB 类型的数据必须使用 PreparedStatement：因为 BLOB 类型的数据时无法使用字符串拼写的。
	 * 调用 setBlob(int index, InputStream inputStream)
	 */
	@Test
	public void testInsertBlob(){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = JDBCUtil.getConnection();
			String sql = "insert into customers(name,email,birth,img) values(?,?,?,?)";
			ps = connection.prepareStatement(sql);
			ps.setString(1, "nameBlob");
			ps.setString(2, "nameBlob@jdbc.com");
			ps.setDate(3, new Date(new java.util.Date().getTime()));
			InputStream is = new FileInputStream("test.jpg");
			ps.setBlob(4, is);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(ps, connection);
		}
	}
	
	/**
	 *  读取 blob 数据: 
	 * 1. 使用 getBlob 方法读取到 Blob 对象
	 * 2. 调用 Blob 的 getBinaryStream() 方法得到输入流。再使用 IO 操作即可. 
	 */
	@Test
	public void testReadBlob(){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = JDBCUtil.getConnection();
			String sql = "select cust_id custId,name Name,birth,img from customers where cust_id =?;";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, 3);
			rs = ps.executeQuery();
			if(rs.next()){
				int id = rs.getInt(1);
				String name = rs.getString("Name");
				String email = rs.getString(3);
				System.out.println(id + "," + name + "," + email);
				Blob pic = rs.getBlob(4);
				InputStream inStream = pic.getBinaryStream();
				OutputStream outStream = new FileOutputStream("out.jpg");
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = inStream.read(buffer)) != -1){
					outStream.write(buffer, 0, len);
				}
				inStream.close();
				outStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			JDBCUtil.release(rs, ps, connection);
		}
	}
	
}
