package jp.natsukishina.orm4j8.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主キーが１つのテーブルにのみ使用できるメソッドにつけるアノテーション<br>
 * このアノテーションがついているメソッドは複合主キーのテーブルには使用できない
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForSinglePrimaryKey {

}
