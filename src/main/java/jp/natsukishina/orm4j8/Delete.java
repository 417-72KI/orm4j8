package jp.natsukishina.orm4j8;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import jp.natsukishina.orm4j8.entity.BaseEntity;

class Delete<E extends BaseEntity> extends AbstractQuery<E> {

	private E element;
	private final List<Object> valueList = new ArrayList<>(0);

	@SuppressWarnings("unchecked")
	Delete(Connection con, E element) {
		super(con, (Class<E>) element.getClass());
		this.element = element;
	}

	@Override
	protected String buildQuery() {
		StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(DBUtil.getTableName4Query(clazz));

		// where
		if (DBUtil.hasPrimaryField(clazz)) {
			List<Field> primaryFields = DBUtil.getPrimaryFields(clazz);
			IntStream.range(0, primaryFields.size()).forEach(idx -> {
				Field primaryField = primaryFields.get(idx);
				if (idx == 0) {
					sql.append(" WHERE ");
				} else {
					sql.append(" AND ");
				}
				sql.append(DBUtil.getColumn4Query(clazz, primaryField));
				sql.append(" = ?");
				valueList.add(DBUtil.getFieldValue(primaryField, element));
			});
		} else {
			List<Field> fieldList = DBUtil.getFieldList(clazz);
			IntStream.range(0, fieldList.size()).forEach(idx -> {
				Field field = fieldList.get(idx);
				if (idx == 0) {
					sql.append(" WHERE ");
				} else {
					sql.append(" AND ");
				}
				sql.append(DBUtil.getColumn4Query(clazz, field));
				if (DBUtil.getFieldValue(field, element) == null) {
					sql.append(" is null");
				} else {
					sql.append(" = ?");
				}
				valueList.add(DBUtil.getFieldValue(field, element));
			});
		}

		return sql.toString();
	}

	@Override
	protected List<Object> buildParamsListForPreparedStatement() {
		return valueList;
	}

	@Override
	public boolean result() {
		try {
			return getStatement().getUpdateCount() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
