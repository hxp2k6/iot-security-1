package br.usp.larc.bnpairings;

/**
 * BNField.java
 *
 * Arithmetic in the finite extension field GF(p)
 *
 * Copyright (C) Paulo S. L. M. Barreto and Geovandro C. C. F. Pereira.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import br.usp.larc.pseudojava.BigInteger;
import br.usp.larc.pseudojava.SecureRandom;

public class BNField {

    public static final String differentFields =
        "Operands are in different finite fields";


    /**
     * BN parameters (singleton)
     */
    BNParams bn;
    
    BigInteger value;
    
    BNField(BNParams bn) {
        this.bn = bn;
        this.value = BNParams._0;
    }

    BNField(BNParams bn, BigInteger val) {
        this.bn = bn;
        this.value = val; // caveat: no modular reduction!
    }

    BNField(BNParams bn, BigInteger val, boolean reduce) {
        this.bn = bn;
        this.value = val;
        if (reduce) {            
            reduce();
        }
    }

    BNField(BNParams bn, SecureRandom rand) {
        this.bn = bn;
        this.value = new BigInteger(bn.p.bitLength(), rand);
        reduce();
    }

    /**
     * Compute a random field element.
     *
     * @param    rand    a cryptographically strong pseudo-random number generator.
     *
     * @return  a random field element.
     */
    public BNField randomize(SecureRandom rand) {
        return new BNField(bn, rand);
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public boolean isOne() {
        return value.compareTo(BNParams._1) == 0;
    }

    public boolean equals(Object u) {
        if (!(u instanceof BNField)) {
            return false;
        }
        BNField v = (BNField)u;
        return bn == v.bn && // singleton comparison
            value.compareTo(v.value) == 0;
    }

    public int compareTo(BigInteger v){
        return value.compareTo(v);
    }

    public int compareTo(BNField v){
        return value.compareTo(v.value);
    }

    public BNField reduce(BigInteger v) {
        if(value.signum() < 0 || value.compareTo(v) == 1){            
            return new BNField(bn, value.mod(v));
        }
        return this;
    }

    public BNField reduce() {
        if(value.signum() < 0 || value.compareTo(bn.p) >= 1){            
            return new BNField(bn, value.mod(bn.p));
        }
        return this;
    }

    public int intValue(){
        return this.intValue();
    }

    /**
     * @return -x
     */
    public BNField negate() {        
        return new BNField(bn, (value.signum() != 0) ? bn.p.subtract(value) : value, false);
    }

    public BNField add(BNField v) {
        if (bn != v.bn) { // singleton comparison
            throw new IllegalArgumentException(differentFields);
        }
        BigInteger r = value.add(v.value);
        if (r.compareTo(bn.p) >= 0) {
            r = r.subtract(bn.p);            
        }
        return new BNField(bn, r, false);
    }

    public BNField add(BigInteger v, boolean reduce) {
        BigInteger s = value.add(v);
        if (s.compareTo(bn.p) >= 0 && reduce) {
            s = s.subtract(bn.p);
        }        
        return new BNField(bn, s, false);
    }

    public BNField subtract(BNField v) {
        if (bn != v.bn) { // singleton comparison
            throw new IllegalArgumentException(differentFields);
        }
        BigInteger r = value.subtract(v.value);
        if (r.signum() < 0) {
            r = r.add(bn.p);
        }
        return new BNField(bn, r, false);
    }

    public BNField subtract(BigInteger v) {
        BigInteger r = value.subtract(v);
        if (r.signum() < 0) {
            r = r.add(bn.p);         
        }        
        return new BNField(bn, r, false);
    }

    public BNField twice(int k) {
        BigInteger r = value;
        while (k-- > 0) {
            r = r.shiftLeft(1);
            if (r.compareTo(bn.p) >= 0) {
                r = r.subtract(bn.p);                
            }            
        }
        return new BNField(bn, r, false);
    }

    public BNField halve() {        
        return new BNField(bn,
            (value.testBit(0) ? value.add(bn.p)  : value).shiftRight(1),
            false);
    }

    /**
     * @param v
     *
     * @return this*v
     */
    public BNField multiply(BNField v) {
        if (this == v) {
            return square();
        }
        if (bn != v.bn) { // singleton comparison
            throw new IllegalArgumentException(differentFields);
        }
        if (isOne() || v.isZero()) {
            return v;
        }
        if (isZero() || v.isOne()) {
            return this;
        }        
        return new BNField(bn, value.multiply(v.value), true);
    }

    /**
     * @param v
     *
     * @return this*v
     */
    public BNField multiply(BigInteger v) {        
        return new BNField(bn, value.multiply(v), true);
    }

    /**
     * returns this*x
     */
    public BNField multiply(int s) {
        return new BNField(bn, value.multiply(BigInteger.valueOf(s)), true);
    }

    /**
     * @return this^2
     */
    public BNField square() {
        if (isZero() || isOne()) {
            return this;
        }
        if(negate().isOne()){
            return negate();
        }

        if (value.signum() == 0) {
            return new BNField(bn,
            	BNParams._0, false);
        }
        
        return new BNField(bn,
            value.modPow(BNParams._2, bn.p),
            false);
    }

    /**
     * (x + yi)^3 = x(x^2 - 3y^2) + y(3x^2 - y^2)i
     */
    public BNField cube() {        
        return square().multiply(value); // mod p
    }

    /**
     * @return (this)^{-1}(modp)
     */
    public BNField inverse() throws ArithmeticException {        
        return new BNField(bn, value.modInverse(bn.p), false);
    }

    /*
     * @param k        the expoent
     * @return (this)^{k}(modp)
     *
     * TODO: improve this method
     */
    public BNField exp(BigInteger k) {
        return new BNField(bn, this.value.modPow(k, this.bn.p));        
    }

    /**
     * Compute a square root of this.
     *
     * @return  a square root of this if one exists, or null otherwise.
     */
    public BNField sqrt() {
        if(this.bn.p.mod(BNParams._4).equals(BNParams._3)){
            return new BNField(bn, this.value.modPow(bn.p.add(BNParams._1).multiply(BNParams._4.modInverse(bn.p)), bn.p));
        }
        else
            throw new RuntimeException("Not implemented efficient square root for the subjacent prime field");
    }

    /**
     * Compute a cube root of this.
     *
     * @return  a cube root of this if one exists, or null otherwise.
     */
    public BNField cbrt() {
        //TODO: Fix this method
        throw new RuntimeException("Not implemented efficient cube root for the subjacent prime field");
    }

    public String toString() {
        return "(" + value + ")";
    }
}



