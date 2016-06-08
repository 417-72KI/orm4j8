package jp.natsukishina.orm4j8.exception;

/**
 * DB関連の例外を扱うクラス
 */
public class DBException extends RuntimeException {

	private static final long serialVersionUID = -5816812484376909732L;

	public DBException() {
		super();
	}

	public DBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DBException(String message, Throwable cause) {
		super(message, cause);
	}

	public DBException(String message) {
		super(message);
	}

	public DBException(Throwable cause) {
		super(cause);
	}


}
