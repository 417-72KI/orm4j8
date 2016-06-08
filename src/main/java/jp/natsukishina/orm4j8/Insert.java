package jp.natsukishina.orm4j8;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.natsukishina.orm4j8.entity.BaseEntity;
import jp.natsukishina.orm4j8.exception.DBException;

class Insert<E extends BaseEntity> extends AbstractQuery<E> {
	private E element;
	private final List<Object> valueList = new ArrayList<>(0);

	@SuppressWarnings("unchecked")
	Insert(Connection con, E element) {
		super(con, (Class<E>) element.getClass());
		this.element = element;
		fieldCompletion();
	}

	private void fieldCompletion() {
		List<Field> primaryFields = DBUtil.getPrimaryFields(element);
		if(primaryFields.size() == 0) {
			return;
		}
		if(primaryFields.stream().allMatch(field -> DBUtil.getFieldValue(field, element) != null)){
			return;
		}

		Field field = DBUtil.getPrimaryField(element);
		if (DBUtil.isInt(field.getType()) || DBUtil.isLong(field.getType())) {
			try {
				field.setAccessible(true);
				final String MAX_PRIMARY_COL = "max_primary";
				StringBuilder sql = new StringBuilder("SELECT MAX(");
				sql.append(DBUtil.getColumn4Query(clazz, field));
				sql.append(") AS ");
				sql.append(DBUtil.getColumn4Query(MAX_PRIMARY_COL));
				sql.append(" FROM ");
				sql.append(DBUtil.getTableName4Query(clazz));
				ResultSet rs = new RawQuery<>(con, clazz, sql.toString()).execute().getResultSet();
				rs.next();
				if (DBUtil.isInt(field.getType())) {
					int maxValue = rs.getInt(MAX_PRIMARY_COL);
					field.set(element, maxValue + 1);
				} else {
					long maxValue = rs.getLong(MAX_PRIMARY_COL);
					field.set(element, maxValue + 1);
				}
			} catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
				throw new DBException(e);
			} finally {
				field.setAccessible(false);
			}
		} else {
			throw new DBException(
					"Primary Field + [" + field.getName() + "] must not be null when its type is not numeric");
		}
	}

	@Override
	public boolean result() {
		try {
			return getStatement().getUpdateCount() != -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected String buildQuery() {
		String tableName = DBUtil.getTableName(clazz);
		Field[] fields = DBUtil.getFields(clazz);

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(tableName);

		// columns
		StringBuilder columns = new StringBuilder("(");
		StringBuilder values = new StringBuilder("(");
		{
			int idx[] = { 0 };
			DBUtil.getFieldList(clazz).forEach(field -> {
				try {
					field.setAccessible(true);
					String column = DBUtil.getColumn(field);
					Object value = field.get(element);
					columns.append(column);
					values.append("?");
					idx[0]++;
					if (idx[0] < fields.length) {
						columns.append(", ");
						values.append(", ");
					}
					if (value == null) {
						valueList.add(field);
					} else {
						valueList.add(value);
					}
				} catch (Exception e) {
					throw new DBException(e);
				} finally {
					field.setAccessible(false);
				}
			});
		}
		columns.append(")");
		values.append(")");

		sql.append(" ");
		sql.append(columns);
		sql.append(" VALUES ");
		sql.append(values);
		sql.append(";");
		return sql.toString();
	}

	@Override
	protected List<Object> buildParamsListForPreparedStatement() {
		return valueList;
	}

}
