package jp.natsukishina.orm4j8;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.natsukishina.orm4j8.annotations.Column;
import jp.natsukishina.orm4j8.annotations.ForSinglePrimaryKey;
import jp.natsukishina.orm4j8.annotations.HasMany;
import jp.natsukishina.orm4j8.annotations.HasOne;
import jp.natsukishina.orm4j8.annotations.UseTable;
import jp.natsukishina.orm4j8.entity.BaseEntity;
import jp.natsukishina.orm4j8.exception.DBException;

/**
 * DB操作に関するユーティリティクラス
 * @author 417.72KI
 *
 */
public class DBUtil {

	public static <E extends BaseEntity> boolean hasTableName(Class<E> clazz) {
		UseTable table = clazz.getDeclaredAnnotation(UseTable.class);
		if (table == null) {
			return true;
		}
		return table.value() != null || !table.value().isEmpty();
	}
	
	static <E extends BaseEntity> String getTableName(E element) {
		return getTableName(element.getClass());
	}

	/**
	 * テーブル名を取得します。<br>
	 * @param clazz 取得するクラス
	 * @return テーブル名
	 * @see jp.natsukishina.orm4j8.annotations.UseTable#value()
	 */
	static <E extends BaseEntity> String getTableName(Class<E> clazz) {
		UseTable table = clazz.getDeclaredAnnotation(UseTable.class);
		if (table == null) {
			return null;
		}

		return table.value();
	}

	public static <E extends BaseEntity> String getTableName4Query(Class<E> clazz) {
		UseTable table = clazz.getDeclaredAnnotation(UseTable.class);
		if (table == null) {
			return null;
		}

		return "`" + table.value() + "`";
	}

	static <E extends BaseEntity> String getTableName4Query(String tableName) {
		return "`" + tableName + "`";
	}


	private static <E extends BaseEntity> Stream<Field> getFieldStream(Class<E> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		return Arrays.stream(fields).filter(f -> f.getDeclaredAnnotation(Column.class) != null);
	}

	/**
	 * 主キーを含むか確認
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 確認するクラス
	 * @return 主キーが含まれていればtrue、なければfalse
	 */
	static <E extends BaseEntity> boolean hasPrimaryField(Class<E> clazz) {
		List<Field> fields = getPrimaryFields(clazz);
		return fields != null && !fields.isEmpty();
	}

	/**
	 * 主キーを含むか確認
	 * @param <E> データベースのテーブルを示す型
	 * @param element 確認するエンティティ
	 * @return 主キーが含まれていればtrue、なければfalse
	 */
	static <E extends BaseEntity> boolean hasPrimaryField(E element) {
		return hasPrimaryField(element.getClass());
	}

	@ForSinglePrimaryKey
	static <E extends BaseEntity> Field getPrimaryField(Class<E> clazz) {
		Optional<Field> field = getFieldStream(clazz).filter(f -> f.getDeclaredAnnotation(Column.class).primary())
				.findFirst();
		if (!field.isPresent()) {
			return null;
		}
		return field.get();
	}

	@ForSinglePrimaryKey
	static <E extends BaseEntity> Field getPrimaryField(E element) {
		return getPrimaryField(element.getClass());
	}

	static <E extends BaseEntity> List<Field> getPrimaryFields(Class<E> clazz) {
		return getFieldStream(clazz).filter(f -> f.getDeclaredAnnotation(Column.class).primary())
				.collect(Collectors.toList());
	}

	static <E extends BaseEntity> List<Field> getPrimaryFields(E element) {
		return getPrimaryFields(element.getClass());
	}

	@ForSinglePrimaryKey
	static <E extends BaseEntity> String getPrimaryColumn(Class<E> clazz) {
		Field primaryField = getPrimaryField(clazz);
		if(primaryField == null) {
			return null;
		}
		return primaryField.getDeclaredAnnotation(Column.class).value();
	}

	@ForSinglePrimaryKey
	static <E extends BaseEntity> String getPrimaryColumn(E element) {
		return getPrimaryColumn(element.getClass());
	}

	static <E extends BaseEntity> List<String> getPrimaryColumns(Class<E> clazz) {
		return getFieldStream(clazz).filter(f -> f.getDeclaredAnnotation(Column.class).primary())
				.map(f -> f.getDeclaredAnnotation(Column.class)).map(c -> c.value()).collect(Collectors.toList());
	}

	static <E extends BaseEntity> List<String> getPrimaryColumns(E element) {
		return getPrimaryColumns(element.getClass());
	}

	static <E extends BaseEntity> List<String> getNonPrimaryColumns(Class<E> clazz) {
		return getFieldStream(clazz).filter(f -> !f.getDeclaredAnnotation(Column.class).primary())
				.map(f -> f.getDeclaredAnnotation(Column.class)).map(c -> c.value()).collect(Collectors.toList());
	}

	static <E extends BaseEntity> List<String> getNonPrimaryColumns(E element) {
		return getNonPrimaryColumns(element.getClass());
	}

	/**
	 * 主キーの値を取得する<br>
	 *
	 * @param <E> データベースのテーブルを示す型
	 * @param element 取得するエンティティ
	 * @return エンティティが持つ主キーの値
	 */
	@ForSinglePrimaryKey
	static <E extends BaseEntity> Object getPrimaryValue(E element) {
		Field primaryField = null;
		try {
			primaryField = getPrimaryField(element);
			if (primaryField == null) {
				return null;
			}

			primaryField.setAccessible(true);
			return primaryField.get(element);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (primaryField != null) {
				primaryField.setAccessible(false);
			}
		}
	}

	static boolean isPrimary(Field field) {
		if (field == null) {
			throw new DBException("invalid field null");
		}
		Column column = field.getDeclaredAnnotation(Column.class);
		if (column == null) {
			throw new DBException("field hasn't annotation Column");
		}

		return column.primary();
	}

	public static <E extends BaseEntity> Field getField(Class<E> clazz, String columnName) {
		try {
			Optional<Field> field = getFieldStream(clazz)
					.filter(f -> columnName.equals(f.getDeclaredAnnotation(Column.class).value())).findFirst();
			return field.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static <E extends BaseEntity> Field getField4Query(Class<E> clazz, String columnName) {
		try {
			Optional<Field> field = getFieldStream(clazz)
					.filter(f -> columnName.equals(getColumn4Query(clazz, f))).findFirst();
			return field.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static <E extends BaseEntity> Object getFieldValue(Field field, E element) {
		try {
			field.setAccessible(true);
			return field.get(element);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DBException(e);
		} finally {
			field.setAccessible(false);
		}
	}

	static <E extends BaseEntity> List<Field> getFieldList(Class<E> clazz) {
		return getFieldStream(clazz).collect(Collectors.toList());
	}

	static <E extends BaseEntity> Field[] getFields(Class<E> clazz) {
		return getFieldList(clazz).toArray(new Field[0]);
	}

	static String getColumn(Field field) {
		Column column = field.getDeclaredAnnotation(Column.class);
		if (column == null) {
			return null;
		}
		return column.value();
	}

	static <E extends BaseEntity> String[] getColumns(Class<E> clazz) {
		return getFieldStream(clazz).map(f -> DBUtil.getColumn(f)).toArray(a -> new String[a]);
	}

	static <E extends BaseEntity> List<String> getColumnList(Class<E> clazz) {
		return getFieldStream(clazz).map(f -> DBUtil.getColumn(f)).collect(Collectors.toList());
	}

	static <E extends BaseEntity> List<String> getColumnList4Query(Class<E> clazz) {
		return getFieldStream(clazz).map(f -> "`" + DBUtil.getTableName(clazz) + "`.`" + DBUtil.getColumn(f) + "`")
				.collect(Collectors.toList());
	}

	static <E extends BaseEntity> String[] getColumns4Query(Class<E> clazz) {
		return getFieldStream(clazz).map(f -> "`" + DBUtil.getTableName(clazz) + "`.`" + DBUtil.getColumn(f) + "`")
				.toArray(a -> new String[a]);
	}

	/**
	 * @param clazz
	 * @param field
	 * @return
	 */
	static <E extends BaseEntity> String getColumn4Query(Class<E> clazz, Field field) {
		return "`" + DBUtil.getTableName(clazz) + "`.`" + DBUtil.getColumn(field) + "`";
	}

	static <E extends BaseEntity> String getColumn4Query(Class<E> clazz, String column) {
		return "`" + DBUtil.getTableName(clazz) + "`.`" + column + "`";
	}

	static <E extends BaseEntity> String getColumn4Query(String column) {
		return "`" + column + "`";
	}

	/**
	 * HasManyリレーションの付いているクラス一覧を取得する
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 取得するクラス
	 * @return HasManyオブジェクトのリスト
	 */
	static <E extends BaseEntity> List<HasMany> getHasManyList(Class<E> clazz) {
		if(clazz == null) {
			return null;
		}

		return Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getDeclaredAnnotation(HasMany.class) != null)
		.map(f -> f.getDeclaredAnnotation(HasMany.class)).collect(Collectors.toList());
	}

	public static <E extends BaseEntity> Field getHasManyField(Class<E> clazz, Class<? extends BaseEntity> targetClass) {
		if(clazz == null) {
			return null;
		}
		if(targetClass == null) {
			return null;
		}
		try {
			return Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getDeclaredAnnotation(HasMany.class) != null)
					.filter(f -> f.getDeclaredAnnotation(HasMany.class).targetClass().equals(targetClass)).findFirst()
					.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * HasManyアノテーション情報を取得する
	 * @param <E> データベースのテーブルを示す型
	 * @param field 取得対象のフィールド
	 * @return HasManyアノテーション情報
	 */
	public static <E extends BaseEntity> HasMany getHasMany(Field field) {
		if(field == null) {
			return null;
		}

		return field.getDeclaredAnnotation(HasMany.class);
	}


	/**
	 * HasOneリレーションの付いているクラス一覧を取得する
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 取得対象のクラス
	 * @return HasOneリレーションの付いているクラス一覧
	 */
	public static <E extends BaseEntity> List<HasOne> getHasOneList(Class<E> clazz) {
		if (clazz == null) {
			return null;
		}

		return getHasOneFieldList(clazz).stream().map(f -> f.getDeclaredAnnotation(HasOne.class))
				.collect(Collectors.toList());
	}

	/**
	 * HasOneリレーションの付いているフィールド一覧を取得する
	 * @param <E> データベースのテーブルを示す型
	 * @param clazz 取得対象のクラス
	 * @return HasOneリレーションの付いているフィールド一覧
	 */
	public static <E extends BaseEntity> List<Field> getHasOneFieldList(Class<E> clazz) {
		if (clazz == null) {
			return null;
		}

		return Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getDeclaredAnnotation(HasOne.class) != null)
				.collect(Collectors.toList());
	}

	/**
	 * HasOneアノテーション情報を取得する
	 * @param <E> データベースのテーブルを示す型
	 * @param field 取得対象のフィールド
	 * @return HasOneアノテーション情報
	 */
	public static <E extends BaseEntity> HasOne getHasOne(Field field) {
		if(field == null) {
			return null;
		}

		return field.getDeclaredAnnotation(HasOne.class);
	}



	public static boolean isInt(Class<?> clazz) {
		return clazz.equals(Integer.class) || clazz.equals(int.class);
	}

	public static boolean isLong(Class<?> clazz) {
		return clazz.equals(Long.class) || clazz.equals(long.class);
	}

	public static boolean isDouble(Class<?> clazz) {
		return clazz.equals(Double.class) || clazz.equals(double.class);
	}

	public static boolean isBoolean(Class<?> clazz) {
		return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
	}

	public static boolean isDate(Class<?> clazz) {
		return clazz.equals(Date.class);
	}

	public static boolean isLocalDate(Class<?> clazz) {
		return clazz.equals(LocalDate.class);
	}

	public static boolean isLocalTime(Class<?> clazz) {
		return clazz.equals(LocalTime.class);
	}

	public static boolean isLocalDateTime(Class<?> clazz) {
		return clazz.equals(LocalDateTime.class);
	}

	public static Date convertDate(java.sql.Date date) {
		if (date == null) {
			return null;
		}
		return toDate(date.toLocalDate());
	}

	public static Date convertDate(java.sql.Time time) {
		if (time == null) {
			return null;
		}
		return toDate(time.toLocalTime());
	}

	public static java.sql.Date convertToSqlDate(Date date) {
		if (date == null) {
			return null;
		}

		return java.sql.Date.valueOf(toLocalDate(date));
	}

	public static java.sql.Time convertToSqlTime(Date date) {
		if (date == null) {
			return null;
		}

		return java.sql.Time.valueOf(toLocalTime(date));
	}

	static LocalDate toLocalDate(Date date) {
		return toLocalDateTime(date).toLocalDate();
	}

	static LocalDate toLocalDate(java.sql.Date date) {
		if (date == null) {
			return null;
		}

		return date.toLocalDate();
	}

	static LocalTime toLocalTime(Date date) {
		return toLocalDateTime(date).toLocalTime();
	}

	static LocalTime toLocalTime(java.sql.Time time) {
		if (time == null) {
			return null;
		}

		return time.toLocalTime();
	}

	static LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	static LocalDateTime toLocalDateTime(java.sql.Timestamp timestamp) {
		if (timestamp == null) {
			return null;
		}

		return timestamp.toLocalDateTime();
	}

	static Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	static Date toDate(LocalDate localDate) {
		return toDate(localDate.atStartOfDay());
	}

	static Date toDate(LocalTime localTime) {
		return toDate(LocalDate.ofEpochDay(0L).atTime(localTime));
	}

	static String getPreparedQuery(PreparedStatement pstmt) {
		String query = pstmt.toString();
		String classNameAndHashCode = pstmt.getClass().getName() + "@" + Integer.toHexString(pstmt.hashCode());
		query = query.replaceFirst(classNameAndHashCode + ": ", "");
		return query;
	}
}
