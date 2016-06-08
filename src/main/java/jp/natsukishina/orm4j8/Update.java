package jp.natsukishina.orm4j8;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import jp.natsukishina.orm4j8.entity.BaseEntity;

class Update<E extends BaseEntity> extends AbstractQuery<E> {
	private E element;
	protected final List<Object> valueList = new ArrayList<>(0);

	@SuppressWarnings("unchecked")
	Update(Connection con, E element) {
		super(con, (Class<E>) element.getClass());
		this.element = element;
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
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(DBUtil.getTableName4Query(clazz));

		// set
		sql.append(" SET ");
		Field[] fields = DBUtil.getFields(clazz);
		IntStream.range(0, fields.length).forEach(idx -> {
			Field field = fields[idx];
			sql.append(DBUtil.getColumn4Query(clazz, field));
			sql.append(" = ?");
			if (idx < fields.length - 1) {
				sql.append(", ");
			}

			Object fieldValue = DBUtil.getFieldValue(field, element);
			if(fieldValue == null) {
				valueList.add(field);
			}else{
				valueList.add(fieldValue);
			}
		});

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
					sql.append(" IS NULL");
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

}
