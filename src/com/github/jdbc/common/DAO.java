package com.github.jdbc.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

/**
 * DAO:Data Access Object
 * @author william
 *
 */
public class DAO {
	
	// insert,update,delete操作都可以包含在其中
	/**
	 * void update(String sql,Object...args);
	 * @param sql
	 * @param args
	 */
	public void update(String sql, Object... args){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0;i < args.length; i++){
				preparedStatement.setObject(i+1, args[i]);
			}
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(preparedStatement, connection);
		}
	}
	
	//查询一条记录，返回对应的对象
	//<T> T get(Class<T> clazz,String sql,Object ...args);
	    //1.获取Connection
		//2.获取PreparedStatement
		//3.填充占位符
		//4.进行查询，得到ResultSet
		//5.若ResultSet中有记录，准备一个Map<String,Object>:键：存放列的别名，值：存放列的值
		//6.得到ResultSetMetaData对象
		//7.处理ResultSet,把指针向下移动一个单位
		//8.由ResultSetMetaData对象得到结果集中有多少列
		//9.有ResultSetMetaData得到每一列的别名，由ResultSet得到具体每一列的值
		//10.填充Map对象
		//11.用反射创建Class对应的对象。
		//12.遍历Map对象，用反射填充对象的属性值：
		//属性名为Map中的key，属性值为Map中的value
	    //返回某条记录的某一个字段的值 或 一个统计的值（一共有多少条记录等）
	/**
	 * 查询一条记录，返回对应的对象
	 * <T> T get(Class<T> clazz,String sql,Object ...args);
	 * @param clazz
	 * @param sql
	 * @param args
	 * @return
	 */
	public <T> T get(Class<T> clazz, String sql, Object...args){
		T entity = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0; i < args.length; i++){
				preparedStatement.setObject(i+1, args[i]);
			}
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				Map<String,Object> values = new HashMap<String,Object>();
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for(int i = 0;i < columnCount; i++){
					String columnLabel = rsmd.getColumnLabel(i+1);
					Object columnValue = resultSet.getObject(i+1);
					values.put(columnLabel,columnValue);
				}
				entity = clazz.newInstance();
				for(Map.Entry<String, Object> entry:values.entrySet()){
					String propertyName = entry.getKey();
					Object value = entry.getValue();
//					ReflectionUtils.setFieldValue(entity, propertyName, value);
					BeanUtils.setProperty(entity,propertyName,value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
		return entity;
	}
	
	/**
	 * //查询多条记录，返回对应的对象的集合
	 * <T> List<T> getForList(Class<T> clazz,String sql,Object...args)；
	 * @param clazz
	 * @param sql
	 * @param args
	 * @return
	 */
	public<T> List<T> getForList(Class<T> clazz,String sql,Object...args){
		List<T> list = new ArrayList<>();
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			//1.得到结果集
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0; i < args.length; i++){
				preparedStatement.setObject(i+1, args[i]);
			}
			resultSet = preparedStatement.executeQuery();
			//2.处理结果集，得到Map的List，其中一个Map对象就是一条记录
			//Map的key为resultSet中列的别名，Map的value为列的值
			List<Map<String,Object>> values = handleResultSet2MapList(resultSet);
			//3.把Map的List转为clazz对应List
			//其中Map的key即为clazz对应的对象的propertyName，
			//而Map的value即为clazz对应的对象的propertyValue
			list = transferMapList2BeanList(clazz, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
		return list;
	}
	/**
	 * 获取结果集的 ColumnLabel 对应的 List
	 * @param resultSet
	 * @return
	 */
	public List<String> getColumnLabels(ResultSet resultSet){
		List<String> labels = new ArrayList<>();
		try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			for(int i = 0;i < rsmd.getColumnCount(); i++){
				labels.add(rsmd.getColumnLabel(i+1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return labels;
	}
	/**
	 * 处理结果集, 得到 Map 的一个 List, 其中一个 Map 对象对应一条记录
	 * @param resultSet
	 * @return
	 */
	public List<Map<String,Object>> handleResultSet2MapList(ResultSet resultSet){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<String> columnLabels = getColumnLabels(resultSet);
		Map<String,Object> map = null;
		try {
			while(resultSet.next()){
				map = new HashMap<>();
				for(String columnLabel:columnLabels){
					Object value = resultSet.getObject(columnLabel);
					map.put(columnLabel,value);
				}
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 把Map的List转为clazz对应List
	 * @param clazz
	 * @param values
	 * @return
	 */
	public<T> List<T> transferMapList2BeanList(Class<T> clazz, List<Map<String,Object>> values){
		List<T> result = new ArrayList<T>();
		T bean = null;
		if(values.size() > 0){
			for(Map<String,Object> map:values){
				try {
					bean = clazz.newInstance();
					String propertyName = null;
					Object value = null;
					for(Map.Entry<String, Object> entry:map.entrySet()){
						propertyName = entry.getKey();
						value = entry.getValue();
						BeanUtils.setProperty(bean, propertyName, value);
					}
					result.add(bean);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * 返回某条记录的某一个字段的值 或 一个统计的值（一共有多少条记录等）
	 * <E>E getForValue(String sql,Object...args);
	 * @param sql
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public<E> E getValue(String sql, Object...args){
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtil.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for(int i = 0; i < args.length; i++){
				preparedStatement.setObject(i+1, args[i]);
			}
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				return (E)resultSet.getObject(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtil.release(resultSet, preparedStatement, connection);
		}
		return null;
	}
	
}
