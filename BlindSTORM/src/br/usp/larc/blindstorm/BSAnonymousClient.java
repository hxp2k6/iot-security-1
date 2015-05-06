/**
 * BSAnonymousClient
 * 
 * The anonymous version of a blind storm client
 * 
 * Version 0.1
 * 
 * @author opaiva
 * */
package br.usp.larc.blindstorm;

import br.usp.larc.blindstorm.exception.BSException;
import br.usp.larc.bnpairings.BNPoint;
import br.usp.larc.pseudojava.BigInteger;

public final class BSAnonymousClient extends BSClient {
	
	@SuppressWarnings("unused")
	private BSAnonymousClient() {}
	
	public BSAnonymousClient(BSSecurityLevel securityLevel, byte[] boothPK)
			throws BSException {
		super(securityLevel, boothPK);
	}
	
	@Override
	protected BigInteger getHash(BNPoint V) {
		return BSUtil.h0(V, this.N);
	}
	
}
