package jp.natsukishina.orm4j8;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import jp.natsukishina.orm4j8.Order.Sort;
import jp.natsukishina.orm4j8.entity.BaseEntity;

/**
 * クエリの汎用インターフェース
 * @author 417.72KI
 *
 */
public interface Query<E extends BaseEntity> {

	/**
	 * 検索条件(AND)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(AND) `E`.`column` = 'actual'</pre>
	 * というクエリを生成します。
	 * @param column カラム名
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> where(String column, Object actual);

	/**
	 * 検索条件(AND)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(AND) `E`.`column` op 'actual'</pre>
	 * というクエリを生成します。<br>
	 * opに使用できるのは'&lt;', '&gt;', '=', 'LIKE'等です
	 * @param column カラム名
	 * @param op 演算子
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> where(String column, String op, Object actual);

	/**
	 * 検索条件(AND)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(AND) `?`.`column` = 'actual'</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します。)
	 * @param clazz クラスオブジェクト
	 * @param column カラム名
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> where(Class<? extends BaseEntity> clazz, String column, Object actual);

	/**
	 * 検索条件(AND)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(AND) `?`.`column` op 'actual'</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します。)<br>
	 * opに使用できるのは'&lt;', '&gt;', '=', 'LIKE'等です
	 * @param clazz クラスオブジェクト
	 * @param column カラム名
	 * @param op 演算子
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> where(Class<? extends BaseEntity> clazz, String column, String op, Object actual);

	/**
	 * 検索条件(OR)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(OR) `E`.`column` op 'actual'</pre>
	 * というクエリを生成します。<br>
	 * opに使用できるのは'&lt;', '&gt;', '=', 'LIKE'等です
	 * @param column カラム名
	 * @param op 演算子
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> orWhere(String column, String op, Object actual);

	/**
	 * 検索条件(OR)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(OR) `E`.`column` = 'actual'</pre>
	 * というクエリを生成します。
	 * @param column カラム名
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> orWhere(String column, Object actual);


	/**
	 * 検索条件(OR)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(OR) `?`.`column` op 'actual'</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します。)<br>
	 * opに使用できるのは'&lt;', '&gt;', '=', 'LIKE'等です
	 * @param clazz クラスオブジェクト
	 * @param column カラム名
	 * @param op 演算子
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> orWhere(Class<? extends BaseEntity> clazz, String column, String op, Object actual);

	/**
	 * 検索条件(OR)を追加します。<br>
	 * 内部では
	 * <pre>WHERE(OR) `?`.`column` = 'actual'</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します)
	 * @param clazz クラスオブジェクト
	 * @param column カラム名
	 * @param actual 期待される値
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> orWhere(Class<? extends BaseEntity> clazz, String column, Object actual);

	/**
	 * ソート対象(昇順)を追加します。<br>
	 * 内部では
	 * <pre>ORDER BY `?`.`column` ASC</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します)
	 * @param clazz クラスオブジェクト
	 * @param column ソート対象カラム名
	 * @return クエリオブジェクト
	 */
	public Query<E> orderBy(Class<? extends BaseEntity> clazz, String column);

	/**
	 * ソート対象(昇順)を追加します。<br>
	 * 内部では
	 * <pre>ORDER BY `E`.`column` ASC</pre>
	 * というクエリを生成します。
	 * @param column ソート対象カラム名
	 * @return クエリオブジェクト
	 */
	public Query<E> orderBy(String column);

	/**
	 * ソート対象を追加します。<br>
	 * 内部では
	 * <pre>ORDER BY `?`.`column` sort</pre>
	 * というクエリを生成します。(?は引数のclazzに依存します)
	 * @param clazz クラスオブジェクト
	 * @param column ソート対象カラム名
	 * @param sort ソート順
	 * @return クエリオブジェクト
	 */
	public Query<E> orderBy(Class<? extends BaseEntity> clazz, String column, Sort sort);

	/**
	 * ソート対象を追加します。<br>
	 * 内部では
	 * <pre>ORDER BY `E`.`column` sort</pre>
	 * というクエリを生成します。
	 * @param column ソート対象カラム名
	 * @param sort ソート順
	 * @return クエリオブジェクト
	 */
	public Query<E> orderBy(String column, Sort sort);

	/**
	 * 開始位置を設定します。<br>
	 * デフォルトは0です。
	 * @param offset 開始位置
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> offset(int offset);

	/**
	 * 最大取得件数を設定します。<br>
	 * デフォルトは無し(無制限)です。
	 * @param limit 最大取得件数
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> limit(int limit);

	/**
	 * 生成したクエリを実行します。
	 * @return クエリオブジェクト
	 */
	public abstract Query<E> execute();

	/**
	 * クエリの実行結果を返します。
	 * @return 実行結果
	 */
	public abstract boolean result();

	/**
	 * 更新カウントとして現在の結果を取得します。
	 * 結果がResultSetオブジェクトであるか、または結果がない場合は -1を返します。
	 * このメソッドは、1つの結果につき1回だけ呼び出す必要があります。
	 * @return 更新カウントしての現在の結果。現在の結果がResultSetオブジェクトであるか、または結果がない場合は -1
	 * @see java.sql.Statement#getUpdateCount()
	 */
	abstract int getUpdateCount();

	/**
	 * ResultSetオブジェクトとして現在の結果を取得します。このメソッドは、1つの結果につき1回だけ呼び出す必要があります。
	 * @return ResultSetオブジェクトとしての現在の結果。結果が更新カウントであるか、または結果がない場合はnull
	 * @see java.sql.Statement#getResultSet()
	 */
	abstract ResultSet getResultSet();

	/**
	 * 検索結果のエンティティを返します。<br>
	 * このメソッドは検索クエリ以外で使用することはできません。
	 * @return 検索結果エンティティ
	 */
	public abstract E get();

	/**
	 * 検索結果のエンティティリストを返します。<br>
	 * このメソッドは検索クエリ以外で使用することはできません。
	 * @return 検索結果エンティティリスト
	 */
	public abstract List<E> getAll();


	/**
	 * クエリ用ビルダークラス
	 * @author 417.72KI
	 *
	 */
	public static class Builder {

		/**
		 * 検索用クエリを生成します。
		 * @param <E> 検索対象テーブルのクラスオブジェクト
		 * @param con コネクション
		 * @param clazz 検索対象テーブルのクラス
		 * @return 検索用クエリ
		 */
		public static <E extends BaseEntity> Query<E> createFindQuery(Connection con, Class<E> clazz) {
			return new Select<>(con, clazz);
		}

		/**
		 * 登録用クエリを生成します。
		 * @param <E> 登録するエンティティのクラス
		 * @param con コネクション
		 * @param element 登録するエンティティ
		 * @return 登録用クエリ
		 */
		public static <E extends BaseEntity> Query<E> createInsertQuery(Connection con, E element) {
			return new Insert<>(con, element);
		}

		/**
		 * 更新用クエリを生成します。
		 * @param <E> 更新するエンティティのクラス
		 * @param con コネクション
		 * @param element 更新するエンティティ
		 * @return 更新用クエリ
		 */
		public static <E extends BaseEntity> Query<E> createUpdateQuery(Connection con, E element) {
			return new Update<>(con, element);
		}

		/**
		 * 削除用クエリを生成します。
		 * @param <E> 削除するエンティティのクラス
		 * @param con コネクション
		 * @param element 削除するエンティティ
		 * @return 削除用クエリ
		 */
		public static <E extends BaseEntity> Query<E> createDeleteQuery(Connection con, E element) {
			return new Delete<>(con, element);
		}

		/**
		 * 生クエリを生成します。
		 * @param <E> 操作するデーブルのクラス
		 * @param con コネクション
		 * @param clazz 操作するデーブルのクラス
		 * @param query 実行するクエリ
		 * @return 生クエリ
		 */
		@Deprecated
		public static <E extends BaseEntity> Query<E> createRawQuery(Connection con, Class<E> clazz, String query) {
			return new RawQuery<>(con, clazz, query);
		}
	}
}
