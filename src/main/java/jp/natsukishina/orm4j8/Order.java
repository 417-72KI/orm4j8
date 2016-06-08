package jp.natsukishina.orm4j8;

/**
 * ORDER BY句を生成するインターフェース
 */
@FunctionalInterface
interface Order<E> {

	String by();

	default Sort sort() {
		return Sort.ASC;
	}

	public enum Sort {
		ASC("asc"), DESC("desc");

		public final String sortQuery;

		private Sort(String sortQuery) {
			this.sortQuery = sortQuery;
		}
	}
}
