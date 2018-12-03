package AuctionClient;

import AuctionInterfaces.*;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.*;
import java.util.Scanner;

/**
 * A parent class to Buyer and Seller which handles common functionality.
 *
 * Handles:
 *   initial connection to the Auction house server
 *   reconnecting to the auction house server at any time
 *   taking user input for prices
 */
public abstract class Client {

  protected static final String RMI_REMOTE_EXCEPTION_STIRNG
          = "Error: unable to communicate with Auction house server";

  protected final Scanner sc = new Scanner(System.in);

  protected AuctionHouse server;
  protected PriceFactory priceFactory;

  protected Bidder bidder;
  protected PrivateKey privateKey;
  protected PublicKey publicKeyServer = (PublicKey) loadSerializedFile("keys/publicKeyS.ser");

  /**
   * initializes connection to the auction house server
   */
  public Client() {
    // Create the reference to the remote object through the rmiregistry
    try {
      server = (AuctionHouse) Naming.lookup("rmi://localhost/AuctionHouse");
      priceFactory = server.getPriceFactory();
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      System.out.println("Error: unable to communicate with Auction house server");
      System.exit(1);
    }

    sc.useDelimiter("\\R");

    while (true) {
      try {
        login();
      } catch (RemoteException e) {
        System.out.println(RMI_REMOTE_EXCEPTION_STIRNG);
        if (reconnectToServer())
          System.out.println("... Reconnected to Auction house server. Please try action again");
        continue;
      }
      break;
    }
  }

  /**
   * Attempts to reconnect to the auction house server
   * @return true if reconnection successful, false otherwise.
   */
  protected boolean reconnectToServer() {
    try {
      server = (AuctionHouse) Naming.lookup("rmi://localhost/AuctionHouse");
      priceFactory = server.getPriceFactory();
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      System.out.println("Error: unable to reconnect to Auction house server");
      return false;
    }
    return true;
  }

  /**
   * Displays a message then takes user input for a price.
   * Does not accept
   *   values >= Integer.max_value/100
   *   negative values
   *   values with more than 2 decimal places
   *
   * @param inputMsg A string to display before requesting user input
   * @return The Price object containing the user inputted value
   */
  protected Price inputPrice(String inputMsg) {
    while (true) {
      System.out.print(inputMsg);
      String input = sc.next().trim();

      // matches any series of digits with optional decimal point and up to 2 digits after
      if (input.matches("\\d+(?:\\.\\d\\d?)?")) {
        Price price;
        try {
          price = priceFactory.createPrice((int) (Float.valueOf(input)*100));
        } catch (IllegalArgumentException e) {
          System.out.println("Error: not a valid price");
          continue;
        }

        return price;
      }

      System.out.println("Error: not a valid price");
    }
  }

  private void authenticate() throws RemoteException {
    authenticate(false);
  }

  private void authenticate(boolean recursiveCall) throws RemoteException {
    int myChallenge = new SecureRandom().nextInt();
    int serverChallenge = 0;
    byte[] serverResponse = null;
    byte[] myResponse = null;

    try {
      Pair<byte[], Integer> response = server.beginAuth(bidder, myChallenge);
      serverChallenge = response.getRight();
      serverResponse = response.getLeft();
    } catch (IllegalStateException e) {
      if (!recursiveCall) {
        // complete the previous session with a dummy response
        // then attempt to start authentication again
        server.finalizeAuth(bidder, new byte[]{0});
        authenticate(true);
        return;
      } else {
        System.out.println("Error: Authentication cannot complete, please try again later");
        System.exit(1);
      }
    }

    try {
      // verify server response
      Signature dsa = Signature.getInstance("SHA1withDSA");
      dsa.initVerify(publicKeyServer);
      dsa.update(BigInteger.valueOf(myChallenge).toByteArray());
      if (!dsa.verify(serverResponse)) {
        System.out.println("Error: Server failed authentication challenge.\nTerminating connection...");
        System.exit(1);
      }

      // respond to server challenge
      dsa.initSign(privateKey);
      dsa.update(BigInteger.valueOf(serverChallenge).toByteArray());
      myResponse = dsa.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      // should never execute
      e.printStackTrace();
      System.exit(1);
    }

    if (server.finalizeAuth(bidder, myResponse))
      System.out.println("Client authenticated with auction house...\n");
    else {
      System.out.println("Error: Client failed authentication challenge.\nTerminating connection...");
      System.exit(1);
    }
  }

  /**
   * takes user input to login to the auction house
   * instantiates the Bidder instance-variable upon completion
   */
  private void login() throws RemoteException {
    while (true) {
      System.out.println("Enter your email address to login, or type \"new\" to create a new account");

      System.out.print("email: ");
      String input = sc.next().trim().toLowerCase();
      if (input.equals("new")) {
        newUser();
        return;
      }
      if (!input.matches("[a-zA-z][\\w.-]*@(?:[a-zA-z][\\w.-]+\\.)+[a-zA-z]{2,4}")) {
        System.out.println("Error: Invalid email address");
        continue;
      }
      bidder = server.getUser(input);
      if (bidder == null) {
        System.out.println("Error: user " + input + " not found");
        continue;
      }
      System.out.println("Enter the file path to your private key");
      System.out.print("file path: ");
      String path = sc.next().trim().toLowerCase();
      if (!path.matches("^(?:[a-z_\\-\\s0-9.])+(?:/[a-z_\\-\\s0-9.]+)+\\.(?:ser|key)$")) {
        System.out.println("Error: not a valid path");
        continue;
      }
      privateKey = (PrivateKey) loadSerializedFile(path);
      if (privateKey == null) {
        System.out.println("Error: private key file not found");
        continue;
      }
      break;
    }

    authenticate();
  }

  /**
   * Takes user input for the bidder's name and email address from the command line
   * Stores the inputted Name and Email within the bidder instance variable.
   */
  private void newUser() throws RemoteException {
    String name;
    String email;

    System.out.println("Input user details:\n");

    while (true) {
      System.out.print("First and last name: ");
      name = sc.next();

      // regular expression for a first and second name separated by a space
      // e.g. John Smith, James O'Connor
      // Does not accept more than 2 names e.g. john jacob jingleheimer schmidt
      if (name.matches("[A-Za-z][a-zA-z'-]*[a-zA-z] [A-Za-z][a-zA-z'-]*[a-zA-z]"))
        break;
      else
        System.out.println("Error: not a valid name. Example name: \"John Smith\"");
    }

    while (true) {
      System.out.print("Email address: ");
      email = sc.next();

      // regular expression for custom email address with 2-4 character top level domain
      if (email.matches("[a-zA-z][\\w.-]*@(?:[a-zA-z][\\w.-]+\\.)+[a-zA-z]{2,4}"))
        break;
      else
        System.out.println("Error: not a valid email address");
    }

    KeyPair pair = null;
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
      keyGen.initialize(1024, SecureRandom.getInstance("SHA1PRNG"));
      pair = keyGen.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      // should never execute
      e.printStackTrace();
      System.exit(1);
    }

    privateKey = pair.getPrivate();
    bidder = server.createBidder(name, email, pair.getPublic());
    if (bidder == null) {
      System.out.println("Error: A user with that email address already exists");
      newUser();
      return;
    }

    saveSerializedFile("keys/privateKey-"+email.split("@")[0]+".key", privateKey);
    System.out.println("Details successfully set");
    System.out.println("private key saved to /keys/privateKey-"+email.split("@")[0]+".key\n");

    authenticate();
  }

  /**
   * Saves a serialized object to a local directory
   * @param fileName the path to the file
   * @param toSave the object to serialize
   */
  private void saveSerializedFile(String fileName, Object toSave) {
    try (
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
      objOut.writeObject(toSave);
    } catch (IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Loads a serialized object from a local directory
   * @param fileName the path to the file to load
   * @return the deserialized object or null if file not found
   */
  private Object loadSerializedFile(String fileName) {
    Object result = null;
    try (
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
      result = objIn.readObject();
    } catch (FileNotFoundException e) {
      return null;
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return result;
  }
}
