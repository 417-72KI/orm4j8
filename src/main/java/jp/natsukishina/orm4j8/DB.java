package jp.natsukishina.orm4j8;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

import org.slf4j.LoggerFactory;

import jp.natsukishina.orm4j8.annotations.ForSinglePrimaryKey;
import jp.natsukishina.orm4j8.entity.BaseEntity;
import jp.natsukishina.orm4j8.exception.DBException;

/**
 * DB操作を扱うクラス。
 * @author 417.72ki
 *
 */
public class DB {
	private static Connection con;

	private static Type type = null;
	private static String host = null;
	private static int port = -1;
	private static String dbName = null;
	private static String user = null;
	private static String pass = null;
	private static String[] option = {};

	/**
	 * 初期設定を行います。<br>
	 * configメソッドはデータベース接続前に必ず行うようにしてください。<br>
	 * 推奨はstaticイニシャライザによる設定です。
	 * @param type データベース種別
	 * @param host データベースサーバのホスト
	 * @param port データベースサーバのポート
	 * @param dbName データベース名
	 * @param user データベースユーザ名
	 * @param pass データベースパスワード
	 * @param option その他オプション(key=value形式)
	 */
	public static void config(Type type, String host, int port, String dbName, String user, String pass,
			String... option) {
		DB.type = type;
		DB.host = host;
		DB.port = port;
		DB.dbName = dbName;
		DB.user = user;
		DB.pass = pass;
		DB.option = option;
		loadDriver();
	}

	/**
	 * 初期設定を行います<br>
	 * 但し、ポートはDBMSのデフォルトポートを使用します
	 * configメソッドはデータベース接続前に必ず行うようにしてください。<br>
	 * 推奨はstaticイニシャライザによる設定です。
	 * @param type データベース種別
	 * @param host データベースサーバのホスト
	 * @param dbName データベース名
	 * @param user データベースユーザ名
	 * @param pass データベースパスワード
	 * @param option その他オプション(key=value形式)
	 */
	public static void config(Type type, String host, String dbName, String user, String pass,
			String... option) {
		config(type, host, type.defaultPort, dbName, user, pass, option);
	}

	private DB() {
	}

	private static Connection getConnection() {
		try {
			if(con == null || con.isClosed()) {
				con = DriverManager.getConnection(createDBUrl(type, host, port, dbName), user, pass);
			}
			return con;
		} catch (Exception e) {
			throw new DBException(e);
		}
	}

	/**
	 * 検索用クエリを発行します。
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 検索対象テーブルのクラス
	 * @return 検索用クエリ
	 */
	public static <E extends BaseEntity> Query<E> find(Class<E> clazz) {
		if(clazz == null) {
			throw new IllegalArgumentException("clazz must not be null");
		}
		return Query.Builder.createFindQuery(getConnection(), clazz);
	}

	/**
	 * 主キーを元にエンティティを検索します
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 検索対象テーブルのクラスオブジェクト
	 * @param primaryKey 主キーの値
	 * @return 検索結果エンティティ
	 */
	@ForSinglePrimaryKey
	public static <E extends BaseEntity> E findByPrimary(Class<E> clazz, Object primaryKey) {
		if(clazz == null) {
			throw new IllegalArgumentException("clazz must not be null");
		}
		if(primaryKey == null) {
			throw new IllegalArgumentException("primaryKey must not be null");
		}
		return find(clazz).where(DBUtil.getPrimaryColumn(clazz), "=", primaryKey.toString()).execute().get();
	}

	/**
	 * エンティティをデータベースに登録・更新します。<br>
	 * @param <E> データベースのテーブルを示す型
	 * @param element 登録するエンティティ
	 * @return 登録結果
	 */
	public static <E extends BaseEntity> boolean save(E element) {
		if (element == null) {
			throw new IllegalArgumentException("element must not be null");
		}
		if (DBUtil.getPrimaryValue(element) == null) {
			return Query.Builder.createInsertQuery(getConnection(), element).execute().result();
		}

		switch (Query.Builder.createUpdateQuery(getConnection(), element).execute().getUpdateCount()) {
		case 0:
			return Query.Builder.createInsertQuery(getConnection(), element).execute().result();
		case -1:
			return false;
		default:
			return true;
		}
	}

	/**
	 * エンティティをDBから削除します
	 * @param <E> データベースのテーブルを示す型
	 * @param element 削除するエンティティ
	 * @return 削除結果
	 */
	public static <E extends BaseEntity> boolean delete(E element) {
		if (element == null) {
			throw new IllegalArgumentException("element must not be null");
		}
		return Query.Builder.createDeleteQuery(getConnection(), element).execute().result();
	}

	@Deprecated
	static <E extends BaseEntity> Query<E> rawQuery(Class<E> clazz, String query) {
		if (clazz == null) {
			throw new IllegalArgumentException("class must not be null");
		}

		if(query == null || query.isEmpty()) {
			throw new IllegalArgumentException("query must not be empty");
		}

		return Query.Builder.createRawQuery(getConnection(), clazz, query);
	}

	private static String createDBUrl(Type type, String host, int port, String dbName) {
		if (port == -1) {
			port = type.defaultPort;
		}
		String url = String.format("jdbc:%1$s%2$s:%3$d/%4$s?%5$s", type.protocol, host, port, dbName,
				StringUtils.join(option, "&"));
		return url;
	}

	/**
	 * トランザクションを開始します<br>
	 * 内部的には{@link java.sql.Connection}インスタンスのautoCommitプロパティをfalseにして<br>
	 * 自動コミットをオフにしているだけです。<br>
	 * 実際にトランザクションが開始されるタイミングはJDBCドライバに依存します。
	 */
	public static void beginTransaction() {
		try {
			if (!getConnection().getAutoCommit()) {
				return;
			}

			getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * トランザクションをコミットします
	 */
	public static void commit() {
		try {
			if (getConnection().getAutoCommit()) {
				return;
			}
			getConnection().commit();
			getConnection().setAutoCommit(true);

		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * トランザクションをロールバックします
	 */
	public static void rollback() {
		try {
			if (getConnection().getAutoCommit()) {
				return;
			}
			getConnection().rollback();
			getConnection().setAutoCommit(true);

		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	static {
		//JVMのシャットダウン時にコネクションの破棄とJDBCドライバーの参照破棄を行う
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LoggerFactory.getLogger(DB.class).debug("shutdown orm4j8");
			closeConnection();
			unloadDriver();
		}));
	}

	/**
	 * JDBCドライバーをロードする<br>
	 * 参照の登録は各ドライバーのstaticイニシャライザに委譲している
	 */
	private static void loadDriver() {
		try {
			Class.forName(type.driverClass);
		} catch (Exception e) {
			throw new DBException(e);
		}
	}

	/**
	 * JDBCドライバーの参照を破棄する
	 */
	private static void unloadDriver() {
		Collections.list(DriverManager.getDrivers()).forEach(d -> {
			try {
				DriverManager.deregisterDriver(d);
			} catch (Exception e) {
				throw new DBException(e);
			}
		});
	}

	/**
	 * コネクションを破棄する
	 */
	private static void closeConnection() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
			con = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * データベース種別を定義するenum型<br>
	 * 各値ごとにドライバクラス、プロトコル、デフォルトポートの情報を格納しています。
	 */
	public enum Type {
		/** MySQL */
		MYSQL("com.mysql.jdbc.Driver", "mysql://", 3306),
		/** PostgreSQL */
		POSTGRES("org.postgresql.Driver", "postgresql://", 5432),
		/** Oracle DB */
		ORACLE("oracle.jdbc.driver.OracleDriver", "oracle://", 1521),
		// SQLITE("org.sqlite.JDBC", "sqlite://", 4983)
		;

		/**	ドライバクラス名 */
		public final String driverClass;
		/** プロトコル */
		public final String protocol;
		/** DBMSで定義されているデフォルトポート */
		public final int defaultPort;

		private Type(String driverClass, String protocol, int defaultPort) {
			this.driverClass = driverClass;
			this.protocol = protocol;
			this.defaultPort = defaultPort;
		}
	}
}
