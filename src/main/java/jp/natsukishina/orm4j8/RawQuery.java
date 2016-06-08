package jp.natsukishina.orm4j8;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.natsukishina.orm4j8.entity.BaseEntity;

class RawQuery<E extends BaseEntity> extends Select<E> {
	private final String query;

	RawQuery(Connection con, Class<E> clazz, String query) {
		super(con, clazz);
		this.query = query;
	}

	@Override
	public Query<E> where(String column, String op, Object actual) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean result() {
		try {
			int updateCount = getStatement().getUpdateCount();
			if (updateCount != -1) {
				return true;
			}
			return getStatement().getResultSet() != null;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected String buildQuery() {
		return query;
	}

	@Override
	protected List<Object> buildParamsListForPreparedStatement() {
		return new ArrayList<>();
	}

}
