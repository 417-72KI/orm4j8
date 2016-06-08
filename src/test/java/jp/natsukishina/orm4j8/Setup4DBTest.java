package jp.natsukishina.orm4j8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.stream.IntStream;

import jp.natsukishina.orm4j8.DB.Type;
import jp.natsukishina.orm4j8.HasOneTestData.HasOneHasOneTestData;

public class Setup4DBTest {

	/**
	 * DB設定 設定情報はtest.propertiesに格納
	 */
	static void config() {
		Properties properties = loadProperties();
		Type type = Type.valueOf(properties.getProperty("type"));
		String host = properties.getProperty("host");
		int port = Integer.parseInt(properties.getProperty("port"));
		String dbName = properties.getProperty("dbName");
		String user = properties.getProperty("user");
		String pass = properties.getProperty("pass");
		String[] option = properties.getProperty("option").split(",");
		DB.config(type, host, port, dbName, user, pass, option);
	}

	private static Properties loadProperties() {
		try(InputStream inputStream = new FileInputStream("src/test/resources/test.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	static final int insertRow = 10;
	static final int manyRow = 3;

	@SuppressWarnings("deprecation")
	static void createTables() {
		StringBuilder sql = new StringBuilder("create table ");
		sql.append(DBUtil.getTableName(TestData.class));
		sql.append(" (");
		sql.append("id int primary key");
		sql.append(", name varchar(20)");
		sql.append(", options varchar(40)");
		sql.append(", birthday date");
		sql.append(", create_at datetime");
		sql.append(", update_at datetime");
		sql.append(");");
		DB.rawQuery(TestData.class, sql.toString()).execute();

		sql = new StringBuilder("create table ");
		sql.append(DBUtil.getTableName(HasOneTestData.class));
		sql.append(" (");
		sql.append("id int primary key");
		sql.append(", name varchar(20)");
		sql.append(", test_data_id int");
		sql.append(");");
		DB.rawQuery(HasOneTestData.class, sql.toString()).execute();

		sql = new StringBuilder("create table ");
		sql.append(DBUtil.getTableName(HasManyTestData.class));
		sql.append(" (");
		sql.append("id int primary key");
		sql.append(", name varchar(20)");
		sql.append(", test_data_id int");
		sql.append(", options varchar(20)");
		sql.append(");");
		DB.rawQuery(HasManyTestData.class, sql.toString()).execute();

		sql = new StringBuilder("create table ");
		sql.append(DBUtil.getTableName(HasOneHasOneTestData.class));
		sql.append(" (");
		sql.append("id int primary key");
		sql.append(", name varchar(20)");
		sql.append(", has_one_test_data_id int");
		sql.append(");");
		DB.rawQuery(HasManyTestData.class, sql.toString()).execute();
}

	@SuppressWarnings("deprecation")
	static void dropTables() {
		StringBuilder sql = new StringBuilder("drop table if exists ");
		sql.append(DBUtil.getTableName(TestData.class));
		sql.append(";");
		DB.rawQuery(TestData.class, sql.toString()).execute();
		sql = new StringBuilder("drop table if exists ");
		sql.append(DBUtil.getTableName(HasManyTestData.class));
		sql.append(";");
		DB.rawQuery(HasManyTestData.class, sql.toString()).execute();
		sql = new StringBuilder("drop table if exists ");
		sql.append(DBUtil.getTableName(HasOneTestData.class));
		sql.append(";");
		DB.rawQuery(HasOneHasOneTestData.class, sql.toString()).execute();
		sql = new StringBuilder("drop table if exists ");
		sql.append(DBUtil.getTableName(HasOneHasOneTestData.class));
		sql.append(";");
		DB.rawQuery(HasOneHasOneTestData.class, sql.toString()).execute();
	}

	@SuppressWarnings("deprecation")
	static void insertTestRecords() {
		IntStream.range(0, insertRow).forEach(idx -> {
			StringBuilder sql = new StringBuilder("insert into ");
			sql.append(DBUtil.getTableName(TestData.class));
			sql.append(" values (");
			sql.append(idx + 1);
			sql.append(", '");
			sql.append("name");
			sql.append(idx + 1);
			sql.append("', '");
			sql.append("hoge");
			sql.append("', '");
			sql.append("1990-06-01");
			sql.append("', '");
			sql.append(DBUtil.toLocalDateTime(new Date()).toString());
			sql.append("', '");
			sql.append(DBUtil.toLocalDateTime(new Date()).toString());
			sql.append("');");
			DB.rawQuery(TestData.class, sql.toString()).execute();

			sql = new StringBuilder("insert into ");
			sql.append(DBUtil.getTableName(HasOneTestData.class));
			sql.append(" values (");
			sql.append(idx + 1);
			sql.append(", '");
			sql.append("name");
			sql.append(idx + 1);
			sql.append("', ");
			sql.append(idx + 1);
			sql.append(");");
			DB.rawQuery(HasOneTestData.class, sql.toString()).execute();

			sql = new StringBuilder("insert into ");
			sql.append(DBUtil.getTableName(HasOneHasOneTestData.class));
			sql.append(" values (");
			sql.append(idx + 1);
			sql.append(", '");
			sql.append("name");
			sql.append(idx + 1);
			sql.append("', ");
			sql.append(idx + 1);
			sql.append(");");
			DB.rawQuery(HasOneHasOneTestData.class, sql.toString()).execute();
});
		IntStream.range(0, insertRow * manyRow).forEach(idx -> {
			StringBuilder sql = new StringBuilder("insert into ");
			sql.append(DBUtil.getTableName(HasManyTestData.class));
			sql.append(" values (");
			sql.append(idx + 1);
			sql.append(", '");
			sql.append("name");
			sql.append(idx + 1);
			sql.append("', ");
			sql.append((idx / manyRow + 1));
			sql.append(", '");
			sql.append("fuga");
			sql.append("');");
			DB.rawQuery(HasManyTestData.class, sql.toString()).execute();
		});
	}

	@SuppressWarnings("deprecation")
	static void deleteTestRecords() {
		StringBuilder sql = new StringBuilder("delete from ");
		sql.append(DBUtil.getTableName(TestData.class));
		sql.append(";");
		DB.rawQuery(TestData.class, sql.toString()).execute();
		sql = new StringBuilder("delete from ");
		sql.append(DBUtil.getTableName(HasOneTestData.class));
		sql.append(";");
		DB.rawQuery(HasOneTestData.class, sql.toString()).execute();
		sql = new StringBuilder("delete from ");
		sql.append(DBUtil.getTableName(HasManyTestData.class));
		sql.append(";");
		DB.rawQuery(HasManyTestData.class, sql.toString()).execute();
		sql = new StringBuilder("delete from ");
		sql.append(DBUtil.getTableName(HasOneHasOneTestData.class));
		sql.append(";");
		DB.rawQuery(HasOneHasOneTestData.class, sql.toString()).execute();
	}

}
