package authTest;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class KeyTest {
  public static void main(String[] args) throws Exception {
    new KeyTest().run();
  }

  private void run() throws Exception {

    PublicKey serverPub = (PublicKey) KeyGen.loadSerializedFile("publicKeyS");
    PrivateKey serverPriv = (PrivateKey) KeyGen.loadSerializedFile("privateKeyS");

    PublicKey alicePub = (PublicKey) KeyGen.loadSerializedFile("publicKeyA");
    PrivateKey alicePriv = (PrivateKey) KeyGen.loadSerializedFile("privateKeyA");

    Signature dsa = Signature.getInstance("SHA1withDSA");

    // alice sends unsigned message
    Message m1 = new Message();
    m1.sender = "Alice";
    m1.challenge = 52;

    // server responds with a signed challenge and response
    Message m2 = new Message();
    m2.sender = "Server";
    m2.challenge = 456;
    dsa.initSign(serverPriv);
    dsa.update(BigInteger.valueOf(m1.challenge).toByteArray());
    m2.response = dsa.sign();

    // alice confirms response, if true then responds to the server's challenge
    Message m3 = new Message();
    m3.sender = "Alice";
    dsa.initVerify(serverPub);
    dsa.update(BigInteger.valueOf(m1.challenge).toByteArray());
    if (!dsa.verify(m2.response)) {
      System.out.println(m2.sender + " failed challenge");
      System.exit(1);
    }
    System.out.println(m2.sender + " successfully responded to challenge");
    dsa.initSign(alicePriv);
    dsa.update(BigInteger.valueOf(m2.challenge).toByteArray());
    m3.response = dsa.sign();

    // server confirms response
    dsa.initVerify(alicePub);
    dsa.update(BigInteger.valueOf(m2.challenge).toByteArray());
    if (!dsa.verify(m3.response)) {
      System.out.println(m3.sender + " failed challenge");
      System.exit(1);
    }
    System.out.println(m3.sender + " successfully responded to challenge");
  }

  public class Message implements Serializable {
    public String sender;
    public int challenge;
    public byte[] response;
  }
}
