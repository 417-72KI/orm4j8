package jp.natsukishina.orm4j8.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * テーブル名を指定するアノテーション
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseTable {

	/**
	 * @return テーブルの物理名
	 */
	String value();
}
