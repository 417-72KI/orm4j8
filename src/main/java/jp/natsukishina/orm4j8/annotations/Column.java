package jp.natsukishina.orm4j8.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * カラム名を指定するアノテーション<br>
 * 指定できるフィールドはオブジェクト型のみ
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * DB上のカラム名
	 * @return カラムの物理名
	 */
	String value();

	/**
	 * 主キーか否か
	 * @return 主キーの場合はtrue
	 */
	boolean primary() default false;

}
