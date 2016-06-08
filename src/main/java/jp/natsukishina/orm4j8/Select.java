package jp.natsukishina.orm4j8;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import jp.natsukishina.orm4j8.annotations.HasOne;
import jp.natsukishina.orm4j8.entity.BaseEntity;

class Select<E extends BaseEntity> extends AbstractQuery<E> {

	private final List<Where<E>> whereList = new ArrayList<>(0);
	private final List<Order<E>> orderList = new ArrayList<>(0);
	private int offset = 0;
	private int limit = -1;

	Select(Connection con, Class<E> clazz) {
		super(con, clazz);
	}

	@Override
	public Query<E> where(Where<E> where) {
		if (where == null) {
			return this;
		}

		if (where.test().length == 3) {
			whereList.add(where);
		}

		return this;
	}

	@Override
	public Query<E> orWhere(Where<E> where) {
		if (where == null) {
			return this;
		}

		Where<E> or = new Where<E>() {
			@Override
			public Object[] test() {
				return where.test();
			}

			@Override
			public boolean or() {
				return true;
			}
		};
		return where(or);
	}

	@Override
	public Query<E> orderBy(Order<E> order) {
		if (order == null) {
			return this;
		}
		if(order.by() == null || order.by().length() == 0) {
			return this;
		}

		orderList.add(order);
		return this;
	}

	@Override
	public Query<E> offset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public Query<E> limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	protected String buildQuery() {
		// select column, from
		StringBuilder selectClause = new StringBuilder("SELECT ");
		StringBuilder fromClause = new StringBuilder(" FROM ");
		selectClause.append(StringUtils.join(DBUtil.getColumns4Query(clazz), ", "));
		fromClause.append(DBUtil.getTableName4Query(clazz));

		// join
		appendHasOneQuery(clazz, selectClause, fromClause);

		// where
		StringBuilder whereClause = new StringBuilder();
		IntStream.range(0, whereList.size()).forEach(idx -> {
			Where<E> where = whereList.get(idx);
			if (idx == 0) {
				whereClause.append(" WHERE ");
			} else if (where.or()) {
				whereClause.append(" OR ");
			} else {
				whereClause.append(" AND ");
			}
			whereClause.append(where.test()[0]);
			whereClause.append(" ");
			whereClause.append(where.test()[1]);
			whereClause.append(" ?");
		});

		// orderby
		StringBuilder orderByClause = new StringBuilder();
		IntStream.range(0, orderList.size()).forEach(idx -> {
			Order<E> order = orderList.get(idx);
			if(idx == 0) {
				orderByClause.append(" ORDER BY ");
			} else {
				orderByClause.append(", ");
			}
			orderByClause.append(order.by());
			orderByClause.append(" ");
			orderByClause.append(order.sort());
		});

		StringBuilder sql = new StringBuilder(selectClause);
		sql.append(fromClause);
		sql.append(whereClause);
		sql.append(orderByClause);
		// limit, offset
		if (limit != -1) {
			sql.append(" LIMIT ");
			sql.append(limit);
		}
		if (offset != 0) {
			sql.append(" OFFSET ");
			sql.append(offset);
		}

		sql.append(";");
		return sql.toString();
	}

	private void appendHasOneQuery(Class<? extends BaseEntity> clazz, StringBuilder selectClause, StringBuilder fromClause) {
		DBUtil.getHasOneList(clazz).stream().forEach(hm -> {
			selectClause.append(", ");
			selectClause.append(StringUtils.join(DBUtil.getColumns4Query(hm.targetClass()), ", "));
			fromClause.append(" LEFT OUTER JOIN ");
			fromClause.append(DBUtil.getTableName4Query(hm.targetClass()));
			fromClause.append(" ON ");
			fromClause.append(DBUtil.getColumn4Query(clazz, hm.targetColumn()));
			fromClause.append("=");
			fromClause.append(DBUtil.getColumn4Query(hm.targetClass(), hm.foreignKey()));
			appendHasOneQuery(hm.targetClass(), selectClause, fromClause);
		});
	}

	@Override
	protected List<Object> buildParamsListForPreparedStatement() {
		List<Object> paramsList = new ArrayList<>();
		IntStream.range(0, whereList.size()).forEach(idx -> {
			Where<E> where = whereList.get(idx);
			try {
				Field field = DBUtil.getField4Query(clazz, (String) where.test()[0]);
				Object fieldValue = where.test()[2];
				if (fieldValue == null) {
					paramsList.add(field);
				} else {
					paramsList.add(fieldValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return paramsList;
	}

	@Override
	public boolean result() {
		try {
			return getStatement().getResultSet().next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public E get() {
		try {
			ResultSet rs = getStatement().getResultSet();
			if (rs == null) {
				return null;
			}
			if (!rs.next()) {
				return null;
			}
			return createObject(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<E> getAll() {
		try {
			ResultSet rs = getStatement().getResultSet();
			if (rs == null) {
				return null;
			}

			List<E> resultList = new ArrayList<>();
			while (rs.next()) {
				resultList.add(createObject(rs));
			}
			return resultList;
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	/**
	 * オブジェクト生成
	 *
	 * @param rs
	 * @return
	 */
	private E createObject(ResultSet rs) {
		return createObject(rs, clazz);
	}

	/**
	 * オブジェクト生成
	 *
	 * @param rs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private E createObject(ResultSet rs, Class<? extends BaseEntity> clazz) {
		Constructor<E> constructor = (Constructor<E>) clazz.getDeclaredConstructors()[0];
		Parameter[] parameters = constructor.getParameters();
		Object[] paramValues = new Object[parameters.length];
		IntStream.range(0, parameters.length).forEach(idx -> {
			Class<?> paramClass = parameters[idx].getType();
			if (DBUtil.isInt(paramClass)) {
				paramValues[idx] = 0;
			} else if (DBUtil.isLong(paramClass)) {
				paramValues[idx] = 0L;
			} else if (DBUtil.isDouble(paramClass)) {
				paramValues[idx] = 0;
			} else if (DBUtil.isBoolean(paramClass)) {
				paramValues[idx] = false;
			} else {
				paramValues[idx] = null;
			}
		});
		try {
			E ret = constructor.newInstance(paramValues);
			DBUtil.getFieldList(clazz).forEach(f -> {
				f.setAccessible(true);
				try {
					String columnName = DBUtil.getColumn(f);
					Object columnValue = rs.getObject(columnName);
					if (columnValue instanceof java.sql.Date) {
						columnValue = DBUtil.toLocalDate((java.sql.Date) columnValue);
						if (f.getType().equals(Date.class)) {
							columnValue = DBUtil.toDate((LocalDate) columnValue);
						}
					} else if (columnValue instanceof java.sql.Time) {
						columnValue = DBUtil.toLocalTime((java.sql.Time) columnValue);
						if (f.getType().equals(Date.class)) {
							columnValue = DBUtil.toDate((LocalTime) columnValue);
						}
					} else if (columnValue instanceof java.sql.Timestamp) {
						columnValue = DBUtil.toLocalDateTime((java.sql.Timestamp) columnValue);
						if (f.getType().equals(Date.class)) {
							columnValue = DBUtil.toDate((LocalDateTime) columnValue);
						}
					}
					f.set(ret, columnValue);
				} catch (SQLException | IllegalAccessException e) {
					e.printStackTrace();
				}
			});

			DBUtil.getHasOneFieldList(clazz).forEach(f -> {
				f.setAccessible(true);
				try {
					HasOne ho = DBUtil.getHasOne(f);
					BaseEntity hoObj = createObject(rs, ho.targetClass());
					f.set(ret, hoObj);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} finally {
					f.setAccessible(false);
				}
			});

			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}