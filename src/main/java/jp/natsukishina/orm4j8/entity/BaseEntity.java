package jp.natsukishina.orm4j8.entity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.IntStream;

import jp.natsukishina.orm4j8.DB;
import jp.natsukishina.orm4j8.DBUtil;
import jp.natsukishina.orm4j8.annotations.HasMany;
import jp.natsukishina.orm4j8.exception.DBException;

/**
 * DBエンティティの基底クラス。<br>
 * DBとやり取りするクラスは全てこのクラスを継承する必要があります。<br>
 * また、その際はUseTableアノテーションを付与しなければなりません。
 * @author 417.72KI
 *
 */
public abstract class BaseEntity {

	/**
	 * デフォルトコンストラクタ
	 */
	public BaseEntity() {
		if(!DBUtil.hasTableName(getClass())) {
			throw new DBException(getClass().getName() + " must have UseTable annotation.");
		}
	}
	
	/**
	 * 1:nリレーションを持つリストフィールドをDBからロードします
	 * @param clazz 1:nの関係になるクラス
	 */
	public void loadHasMany(Class<? extends BaseEntity> clazz) {
		Field hmField = DBUtil.getHasManyField(getClass(), clazz);
		if(hmField == null) {
			throw new DBException(getClass() + " doesn't have HasMany field");
		}
		if(!hmField.getType().equals(List.class)) {
			throw new DBException("HasMany field must be List");
		}

		hmField.setAccessible(true);
		HasMany hm = DBUtil.getHasMany(hmField);
		Field targetField = null;
		try {
			targetField = DBUtil.getField(getClass(), hm.targetColumn());
			targetField.setAccessible(true);
			Object targetValue = targetField.get(this);
			List<? extends BaseEntity> hmList = DB.find(hm.targetClass()).where(hm.foreignKey(), "=", targetValue)
					.execute().getAll();
			hmField.set(this, hmList);
		} catch (Exception e) {
			throw new DBException(e);
		} finally {
			if (targetField != null) {
				targetField.setAccessible(false);
			}
			if (hmField != null) {
				hmField.setAccessible(false);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append("[\n");
		Field[] fields = getClass().getDeclaredFields();
		IntStream.range(0, fields.length).forEach(idx -> {
			Field field = fields[idx];
			field.setAccessible(true);
			try {
				builder.append("\t");
				builder.append(field.getName());
				builder.append(" : ");
				builder.append(field.get(this));
				if (idx < fields.length) {
					builder.append(",");
				}
				builder.append("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		builder.append("]\n");
		return builder.toString();
	}
}
