package br.usp.larc.keccak;

public class KeccakTest {

    public static void displayBuf(String tag, byte[] z) {
        System.out.print(tag + " = ");
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < z.length; i++) {
            System.out.print(hex.charAt((z[i] >> 4) & 0xf));
            System.out.print(hex.charAt((z[i]     ) & 0xf));
        }
        System.out.println();
    }

    public static void testDuplexing(int hashbitlen, byte[] sigma, int sigmalen) {
        System.out.println("Keccak Duplexing Test @ hashbitlen = " + hashbitlen);
        Keccak k = new Keccak();
        k.init(hashbitlen);

        k.startDebug();
        
        byte[] z0 = k.duplexing(sigma, sigmalen, null, 128);
        displayBuf("z0                ", z0);

        
        //sigma[0] = 0x7B;
        //for(int i=1; i<=125 ;i++)
        //    sigma[i] = 0x00;
        //sigma[126] = 0x7B;

        //for(int i=0; i<=126 ;i++){
        //    sigma[i] = (byte)0xFF;
        //}

        sigma[0] = 0x00;
        sigma[1] = 0x7A;

        for(int i=2; i<=126 ;i++){
            sigma[i] = (byte)0xFF;
        }

        byte[] z1 = k.duplexing(sigma, sigmalen, null, 128);
        displayBuf("z1                ", z1);

    }

    public static void testHash(int hashbitlen, byte[] msg, int len) {
        System.out.println("Keccak Hash Test @ hashbitlen = " + hashbitlen);
        Keccak k = new Keccak();
        k.init(hashbitlen);
        System.out.println("bitrate     = " + k.getBitRate());
        System.out.println("capacity    = " + k.getCapacity());
        System.out.println("diversifier = " + k.diversifier);
        k.update(msg, len*8);
        byte[] h = k.getHash(null);
        System.out.println("Len = " + len);
        displayBuf("Msg", msg);
        displayBuf("MD", h);
        //displayBuf("DQ", k.dataQueue);
    }

    public static void main(String[] args) {
        //testHash(256, new byte[] {(byte)0x53, (byte)0x58, (byte)0x7B, (byte)0xC8}, 29);
        //testHash(256, new byte[] {0x00}, 0);
        //byte[] msg = new byte[136];
        //Arrays.fill(msg, (byte)0);
        //msg[135] = (byte)0x81;
        //displayBuf("M", msg);
        //testHash(256, msg, msg.length);
        //testDuplexing(256, msg, (msg.length-1));

        /*
        byte[] msg = new byte[272];
        Arrays.fill(msg, (byte)0);
        msg[135] = (byte)0x81;
        msg[271] = (byte)0x81;
        displayBuf("M", msg);
        testHash(256, msg, msg.length);
        //*/

        //*
        //byte[] sigma0 = new byte[136];  //SHA3-256
        //byte[] sigma0 = new byte[72];   //SHA3-512
        byte[] sigma0 = new byte[128];    //SHA3-0 (default)

        //Arrays.fill(sigma0, (byte)0);
        //sigma0[0] = 0x7A;
        
        //sigma0[0] = 0x7A;
        //for(int i=1; i<=125 ;i++)
        //    sigma0[i] = 0x00;
        //sigma0[126] = 0x7A;

        //for(int i=0; i<=126 ;i++){
        //    sigma0[i] = (byte)0xFF;
        //}
        String strMessage = "mrriceSchnorr";
        sigma0 = strMessage.getBytes();

        displayBuf("sigma0", sigma0);
        testDuplexing(0, sigma0, 13);
        //*/
        
    }
}
