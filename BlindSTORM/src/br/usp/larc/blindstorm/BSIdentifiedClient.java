/**
 * BSIdentifiedClient
 * 
 * The identified version of a blind storm client
 * 
 * Version 0.1
 * 
 * @author opaiva
 * */
package br.usp.larc.blindstorm;

import br.usp.larc.blindstorm.exception.BSException;
import br.usp.larc.bnpairings.BNPoint;
import br.usp.larc.pseudojava.BigInteger;

public final class BSIdentifiedClient extends BSClient {
	
	private byte[] clientId;
	
	@SuppressWarnings("unused")
	private BSIdentifiedClient() {}
	
	public BSIdentifiedClient(BSSecurityLevel securityLevel, byte[] boothPK,
			byte[] clientId) throws BSException {
		super(securityLevel, boothPK);
		
		if (clientId == null) throw new BSException("Invalid parameters!");

		this.clientId = clientId;
	}
	
	public byte[] getClientId() {
		return clientId;
	}
	
	@Override
	protected BigInteger getHash(BNPoint V) {
		return BSUtil.h1(clientId, V, this.N);
	}
	
}
