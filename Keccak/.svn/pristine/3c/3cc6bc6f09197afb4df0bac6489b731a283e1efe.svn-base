package br.usp.larc.keccak;

public interface SpongeHash {

    /**
     * Prepare to hash a new message.
     *
     * @param  hashBits    the desired hash size in bits.
     */
    void init(int hashBits);

    /**
     * Update the hash computation with a message chunk.
     *
     * @param  aData       data chunk to authenticate.
     * @param  aLength     its length in bytes.
     */
    void update(byte[] aData, int aLength);

    /**
     * Complete if necessary the data processing and
     * get the hash value of the whole message provided.
     *
     * @param  val         the hash value buffer.
     *
     * @return hash value of the whole message.
     *         If val is null, a new buffer is allocated,
     *         otherwise the input buffer is returned.
     */
    byte[] getHash(byte[] val);

}
