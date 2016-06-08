package jp.natsukishina.orm4j8;

/**
 * WHERE句を生成するインターフェース
 */
@FunctionalInterface
interface Where<E> {

	/**
	 * @return [Column Name, op, value]
	 */
	Object[] test();

	/**
	 * @return orで繋ぐ場合はtrue、andで繋ぐ場合はfalse
	 */
	default boolean or() {
		return false;
	}
}
