/**
 * BSInvalidMessageException
 * 
 * An exception thrown at the method unsigncrypt, from BSClient class.
 * 
 * Version 0.1
 * 
 * @author opaiva
 * */
package br.usp.larc.blindstorm.exception;

public class BSInvalidMessageException extends Exception {

	private static final long serialVersionUID = 817387994292054008L;

	private String failedMessage;

	public BSInvalidMessageException(String message, String failedMessage){
		super(message);
		this.failedMessage = failedMessage;
	}

	public String getFailedMessage() {
		return this.failedMessage;
	}

}
