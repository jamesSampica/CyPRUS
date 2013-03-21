package support;

/**
 * Defines methods appropriate for abstract messaging
 * @author James
 *
 */
public interface MessageClient {

	/**
	 * A method handler for writes that occur during a write error
	 * @param e
	 */
	public void errorOnWrite( Exception e );
	/**
	 * A method handler for writes that occur during a read error
	 * @param e
	 */
	public void errorOnRead( Exception e );
	
	/**
	 * A method handler for the reception of a message
	 * @param message the content of the message in bytes
	 */
	public void onMessage( byte[] message );
	
}
