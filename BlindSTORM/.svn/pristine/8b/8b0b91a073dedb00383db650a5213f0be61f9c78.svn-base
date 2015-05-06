/**
 * BSBooth
 * 
 * A class designed to hold the booth master key and the public parameters of a
 * Blind Storm client.
 * 
 * Version 0.1
 * 
 * @author opaiva
 * */
package br.usp.larc.blindstorm;

import br.usp.larc.blindstorm.exception.BSException;
import br.usp.larc.bnpairings.BNPoint;
import br.usp.larc.pseudojava.BigInteger;

public class BSBooth extends BSEntity {
	
	protected BigInteger x; //That's the master key
	
	public class BSKeySigner {
		
		//Thats's a value maintained between a commit and a sign call 
		protected BigInteger uLinha_A;
		
		//A boolean indicating if a commit message was called at least once in this
		//client
		protected boolean commitCalled = false;
		
		private BSKeySigner() {}
		
		/**
		 * Returns the commit message, to be sent to the client blind it
		 * 
		 * @throws BSException
		 * */
		public byte[] getCommit() {
			commitCalled = true;
			
			uLinha_A = BSUtil.randomBigInteger(
					BSSecurityLevel.securityLevelToKeyLength(securityLevel)
					);
			BNPoint VLinha_A = G.multiply(uLinha_A);

			return VLinha_A.toByteArray(BNPoint.COMPRESSED);
		}
		
		/**
		 * Given a commit message blinded by the client, signs it. The sign message
		 * should be returned to the client, in order to it extracts its private key 
		 * 
		 * @throws BSException
		 * */
		public byte[] sign(byte[] blind) throws BSException {
			if (!commitCalled)
				throw new BSException("Commit never called on this client!");
			
			BigInteger hLinha_A = new BigInteger(blind);

			BigInteger sLinha_A = uLinha_A.subtract(hLinha_A.multiply(x)).mod(N);
			
			return BSUtil.adjustBigIntegerConversion(sLinha_A, securityLevel);
		}
		
	}
	
	@SuppressWarnings("unused")
	private BSBooth() {}
	
	public BSBooth(BSSecurityLevel securityLevel, byte[] boothSK)
			throws BSException {
		super(securityLevel);
		
		this.x = new BigInteger(boothSK).mod(N);
		this.Y = G.multiply(x);
	}
	
	/**
	 * Get a key signer to sign a client's autocertificated key
	 * 
	 * @return A new key signer
	 * @throws BSException 
	 * */
	public BSKeySigner getKeySigner() {
		return new BSKeySigner();
	}
	
	/**
	 * Returns the booth master key, used to sign the client's keys
	 * */
	public byte[] getMasterKey() {
		
		return BSUtil.adjustBigIntegerConversion(x, securityLevel);
	}
	
	/**
	 * Returns the booth public key, that the clients must know
	 * */
	public byte[] getPublicKey() throws BSException {
		return Y.toByteArray(
				BSSecurityLevel.securityLevelToKeyLength(securityLevel)
				);
	}
	
}
