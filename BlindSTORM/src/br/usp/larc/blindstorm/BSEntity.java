package br.usp.larc.blindstorm;

import java.util.HashMap;

import br.usp.larc.blindstorm.exception.BSException;
import br.usp.larc.bnpairings.BNCurve;
import br.usp.larc.bnpairings.BNParams;
import br.usp.larc.bnpairings.BNPoint;
import br.usp.larc.bnpairings.data.BN158;
import br.usp.larc.bnpairings.data.BN190;
import br.usp.larc.bnpairings.data.BN222;
import br.usp.larc.bnpairings.data.BN254;
import br.usp.larc.pseudojava.BigInteger;

abstract class BSEntity {
	
	//A class to statically hold the specific security-level parameters
	protected static final class BSParameters {
		BNCurve E;
		BigInteger N;
		BNPoint G;
		
		protected BSParameters(BNCurve E, BigInteger N, BNPoint G) {
			this.E = E;
			this.N = N;
			this.G = G;
		}
	}

	//A map from security levels to parameters sets
	private static final HashMap<BSSecurityLevel, BSParameters>
	securityLevelToBSPameters = new HashMap<BSSecurityLevel, BSParameters>();
	
	//The client security level
	protected BSSecurityLevel securityLevel;
	
	protected BNCurve E;
	protected BigInteger N;
	protected BNPoint G;
	
	//The Booth public key
	protected BNPoint Y;
	
	protected BSEntity() {}
	
	/**
	 * Returns the parameters set associated with a security level
	 * 
	 * @param securityLevel		The security level required
	 * @return the set of parameters for this security level
	 * @throws BSException 
	 * */
	protected BSEntity(BSSecurityLevel securityLevel) throws BSException {
		if (securityLevel == null)
			throw new BSException("Invalid parameters!");
		
		this.securityLevel = securityLevel;
		
		BSParameters params = securityLevelToBSPameters.get(securityLevel); 
		
		if (params == null) {
			BNParams bnParams = null;
			String[] Gtab = null;

			switch (securityLevel) {
				case SECURITY_LEVEL_80:
					bnParams = new BNParams(158);
					Gtab = BN158.Gtab;
					break;
				case SECURITY_LEVEL_96:
					bnParams = new BNParams(190);
					Gtab = BN190.Gtab;
					break;
				case SECURITY_LEVEL_112:
					bnParams = new BNParams(222);
					Gtab = BN222.Gtab;
					break;
				case SECURITY_LEVEL_128:
					bnParams = new BNParams(254);
					Gtab = BN254.Gtab;
					break;
			}

			BNCurve E = new BNCurve(bnParams);
			BigInteger N = E.getOrder();
			BNPoint G = E.getCurveGenerator();

			G.loadCompiledTable(Gtab);

			params = new BSParameters(E, N, G);
			securityLevelToBSPameters.put(securityLevel, params);
		}
		
		this.E = params.E;
		this.N = params.N;
		this.G = params.G;
	}
	
	public BSSecurityLevel getSecurityLevel() {
		return securityLevel;
	}
	
	/**
	 * Given a message prepared for non-repudiation test, the sender public of
	 * this message and its receiver's public key and a security level, returns
	 * true if the message was truly signed by that receiver to that sender.
	 * 
	 * @param message		The bytes from the message to be tested
	 * @param senderPK		The sender's public key bytes
	 * @param receiverPK	The receiver's public key bytes
	 * @param securityLevel A security level to operate at
	 * @return true if the message was truly signed from that receiver to that
	 * sender
	 * */
	public boolean nonRepudiationTest(byte[] message, byte[] senderPK,
			byte[] receiverPK) {
		byte[][] deserialized = BSUtil.deserialize(message);

		byte[] m = deserialized[0];
		BigInteger h = new BigInteger(deserialized[1]);
		BigInteger z = new BigInteger(deserialized[2]);
		byte[] tal = deserialized[3];

		BNPoint y_Sender = new BNPoint(E, senderPK);
		BNPoint y_Receiver = new BNPoint(E, receiverPK);

		BNPoint r = G.multiply(z).add(y_Sender.multiply(h));
		BigInteger w = BSUtil.h2(r, m, y_Sender, y_Receiver, tal, N);

		return w.equals(h);
	}

}
