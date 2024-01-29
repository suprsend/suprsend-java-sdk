package suprsend;

/**
 * Signals that an Suprsend exception of some sort has occurred. This class is
 * the general class of exceptions produced by failed or interrupted Suprsend
 * operations.
 *
 */
public class SuprsendException extends Exception {
	private static final long serialVersionUID = 1L;

	public int statusCode;

	/**
	 * Constructs an {@code SuprsendException} with {@code null} as its error detail
	 * message.
	 */
	public SuprsendException() {
		super();
	}

	/**
	 * Constructs an {@code SuprsendException} with the specified detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by the
	 *                {@link #getMessage()} method)
	 */
	public SuprsendException(String message) {
		super(message);
		this.statusCode = 500;
	}

	public SuprsendException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Constructs an {@code SuprsendException} with the specified detail message and
	 * cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by the
	 *                {@link #getMessage()} method)
	 *
	 * @param cause   The cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method). (A null value is permitted, and
	 *                indicates that the cause is nonexistent or unknown.)
	 *
	 * @since 1.6
	 */
	public SuprsendException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code SuprsendException} with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which typically
	 * contains the class and detail message of {@code cause}). This constructor is
	 * useful for IO exceptions that are little more than wrappers for other
	 * throwable.
	 *
	 * @param cause The cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method). (A null value is permitted, and
	 *              indicates that the cause is nonexistent or unknown.)
	 *
	 * @since 1.6
	 */
	public SuprsendException(Throwable cause) {
		super(cause);
	}
}
