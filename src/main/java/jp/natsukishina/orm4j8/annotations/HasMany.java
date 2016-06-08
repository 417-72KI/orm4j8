package jp.natsukishina.orm4j8.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.natsukishina.orm4j8.entity.BaseEntity;

/**
 * 1:nのリレーションを示すアノテーション<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HasMany {

	/**
	 * リレーション対象となるモデルのクラスオブジェクトを示す
	 * @return 対象クラスオブジェクト
	 */
	Class<? extends BaseEntity> targetClass();

	/**
	 * リレーション対象で使用する外部キー
	 * @return 外部キー名
	 */
	String foreignKey();

	/**
	 * 外部キーの対象カラム
	 * @return 対象カラム
	 */
	String targetColumn() default "id";

	//TODO CASCADEの調査
	//	boolean cascade() default false;
}
