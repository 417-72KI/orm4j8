package jp.natsukishina.orm4j8;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.natsukishina.orm4j8.Order.Sort;
import jp.natsukishina.orm4j8.entity.BaseEntity;
import jp.natsukishina.orm4j8.exception.DBException;

abstract class AbstractQuery<E extends BaseEntity> implements Query<E> {
	protected final Connection con;
	protected final Class<E> clazz;
	private PreparedStatement pstmt;
	private final Logger log = LoggerFactory.getLogger(getClass());

	AbstractQuery(Connection con, Class<E> clazz) {
		this.con = con;
		this.clazz = clazz;
	}

	/**
	 * 検索条件(AND)
	 * @param where
	 * @return
	 */
	protected Query<E> where(Where<E> where) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<E> where(Class<? extends BaseEntity> clazz, String column, String op, Object actual) {
		Object[] where;
		if(actual == null) {
			where = new Object[]{DBUtil.getColumn4Query(clazz, column), "is", null};
		} else {
			where = new Object[]{DBUtil.getColumn4Query(clazz, column), op, actual};
		}
		return where(() -> where);
	}

	@Override
	public Query<E> where(Class<? extends BaseEntity> clazz, String column, Object actual) {
		return where(clazz, column, "=", actual);
	}

	@Override
	public Query<E> where(String column, String op, Object actual) {
		return where(clazz, column, op, actual);
	}

	@Override
	public Query<E> where(String column, Object actual) {
		return where(column, "=", actual);
	}

	protected Query<E> orWhere(Where<E> where) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<E> orWhere(Class<? extends BaseEntity> clazz, String column, String op, Object actual) {
		return orWhere(() -> new Object[]{DBUtil.getColumn4Query(clazz, column), op, actual});
	}

	@Override
	public Query<E> orWhere(Class<? extends BaseEntity> clazz, String column, Object actual) {
		return orWhere(clazz, column, "=", actual);
	}

	@Override
	public Query<E> orWhere(String column, String op, Object actual) {
		return orWhere(clazz, column, op, actual);
	}

	@Override
	public Query<E> orWhere(String column, Object actual) {
		return orWhere(column, "=", actual);
	}

	/**
	 * ソート対象(昇順)
	 *
	 * @param order
	 * @return
	 */
	protected Query<E> orderBy(Order<E> order) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ソート対象
	 * @param order
	 * @param sort
	 * @return
	 */
	protected Query<E> orderBy(Order<E> order, Sort sort) {
		if (order == null) {
			return this;
		}

		Order<E> by = new Order<E>() {
			@Override
			public String by() {
				return order.by();
			}

			@Override
			public Sort sort() {
				return sort;
			}
		};
		return orderBy(by);
	}

	@Override
	public Query<E> orderBy(Class<? extends BaseEntity> clazz, String column) {
		return orderBy(() -> DBUtil.getColumn4Query(clazz, column));
	}

	@Override
	public Query<E> orderBy(String column) {
		return orderBy(clazz, column);
	}

	@Override
	public Query<E> orderBy(Class<? extends BaseEntity> clazz, String column, Sort sort) {
		return orderBy(() -> DBUtil.getColumn4Query(clazz, column), sort);
	}

	@Override
	public Query<E> orderBy(String column, Sort sort) {
		return orderBy(clazz, column, sort);
	}

	@Override
	public Query<E> offset(int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<E> limit(int limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<E> execute() {
		try {
			String rawQuery = buildQuery();
			log.debug("rawQuery: " + rawQuery);
			pstmt = con.prepareStatement(rawQuery);
			List<Object> paramsList = buildParamsListForPreparedStatement();
			log.debug("params: " + paramsList);
			IntStream.range(0, paramsList.size()).forEach(idx -> {
				try {
					Object obj = paramsList.get(idx);
					Class<?> fieldClass = obj.getClass();
					if (fieldClass.equals(Field.class)) {
						fieldClass = ((Field) obj).getType();
						if (DBUtil.isInt(fieldClass)) {
							pstmt.setNull(idx + 1, Types.INTEGER);
						} else if (DBUtil.isLong(fieldClass)) {
							pstmt.setNull(idx + 1, Types.BIGINT);
						} else if (DBUtil.isDouble(fieldClass)) {
							pstmt.setNull(idx + 1, Types.DOUBLE);
						} else if (DBUtil.isBoolean(fieldClass)) {
							pstmt.setNull(idx + 1, Types.BOOLEAN);
						} else if (DBUtil.isDate(fieldClass)) {
							pstmt.setNull(idx + 1, Types.DATE);
						} else if (DBUtil.isLocalDate(fieldClass)) {
							pstmt.setNull(idx + 1, Types.DATE);
						} else if (DBUtil.isLocalTime(fieldClass)) {
							pstmt.setNull(idx + 1, Types.TIME);
						} else if (DBUtil.isLocalDateTime(fieldClass)) {
							pstmt.setNull(idx + 1, Types.TIMESTAMP);
						} else {
							pstmt.setNull(idx + 1, Types.VARCHAR);
						}
					} else if (DBUtil.isInt(fieldClass)) {
						pstmt.setInt(idx + 1, (int) obj);
					} else if (DBUtil.isLong(fieldClass)) {
						pstmt.setLong(idx + 1, (long) obj);
					} else if (DBUtil.isDouble(fieldClass)) {
						pstmt.setDouble(idx + 1, (double) obj);
					} else if (DBUtil.isBoolean(fieldClass)) {
						pstmt.setBoolean(idx + 1, (boolean) obj);
					} else if (DBUtil.isDate(fieldClass)) {
						pstmt.setDate(idx + 1, DBUtil.convertToSqlDate((Date) obj));
					} else if (DBUtil.isLocalDate(fieldClass)) {
						pstmt.setDate(idx + 1, java.sql.Date.valueOf((LocalDate) obj));
					} else if (DBUtil.isLocalTime(fieldClass)) {
						pstmt.setTime(idx + 1, java.sql.Time.valueOf((LocalTime) obj));
					} else if (DBUtil.isLocalDateTime(fieldClass)) {
						pstmt.setTimestamp(idx + 1, java.sql.Timestamp.valueOf((LocalDateTime) obj));
					} else {
						pstmt.setString(idx + 1, (String) obj);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			log.info(DBUtil.getPreparedQuery(pstmt));
			pstmt.execute();
			return this;
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * クエリ文字列生成
	 *
	 * @return
	 */
	protected abstract String buildQuery();

	@Override
	public int getUpdateCount() {
		try {
			return pstmt.getUpdateCount();
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	@Override
	public ResultSet getResultSet() {
		try {
			return pstmt.getResultSet();
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	/**
	 * ステートメントにセットするパラメータリストを生成
	 *
	 * @return
	 */
	protected abstract List<Object> buildParamsListForPreparedStatement();

	protected Statement getStatement() {
		if (pstmt == null) {
			throw new DBException();
		}
		return pstmt;
	}

	@Override
	public E get() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> getAll() {
		throw new UnsupportedOperationException();
	}


	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	protected boolean close() {
		try {
			if (pstmt != null && !pstmt.isClosed()) {
				pstmt.close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
