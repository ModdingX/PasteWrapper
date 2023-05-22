package org.moddingx.pastewrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

// https://support.google.com/cloudidentity/answer/6342198?hl=en
public class EditKeyManager {
    
    private final KeyPair keyPair;

    private EditKeyManager(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
    
    public String getEditToken(String pasteId) throws IOException {
        try {
            Signature sig = Signature.getInstance("NONEwithRSA");
            sig.initSign(this.keyPair.getPrivate());
            sig.update(pasteId.getBytes(StandardCharsets.UTF_8));
            return key(sig.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IOException("Failed to generate edit token", e);
        }
    }

    public String getPasteId(String editToken) throws IOException {
        try {
            byte[] data = data(editToken);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPublic());
            byte[] decrypted = cipher.doFinal(data);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Invalid edit token", e);
        } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to get paste id", e);
        }
    }
    
    private static String key(byte[] data) {
        byte[] fullData = new byte[data.length + 4];
        System.arraycopy(data, 0, fullData, 0, data.length);
        fullData[data.length] = (byte) ((data.length >>> 24) & 0xFF);
        fullData[data.length + 1] = (byte) ((data.length >>> 16) & 0xFF);
        fullData[data.length + 2] = (byte) ((data.length >>> 8) & 0xFF);
        fullData[data.length + 3] = (byte) (data.length & 0xFF);
        return new BigInteger(1, fullData).toString(36);
    }
    
    private static byte[] data(String key) throws IOException {
        BigInteger number;
        try {
            number = new BigInteger(key, 36);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid code: Not a valid number.");
        }
        byte[] rawData = number.toByteArray();
        if (rawData.length < 4) throw new IOException("Invalid code: Too short.");
        int len = (rawData[rawData.length - 4] << 24) | (rawData[rawData.length - 3] << 16) | (rawData[rawData.length - 2] << 8) | ((int) rawData[rawData.length - 1]);
        if (len < 0) throw new IOException("Invalid code: Negative length.");
        byte[] data = new byte[len];
        int amount = Math.min(rawData.length - (rawData[0] == 0 ? 5 : 4), data.length);
        System.arraycopy(rawData, rawData.length - 4 - amount, data, data.length - amount, amount);
        return data;
    }
    
    public static EditKeyManager create(Path publicKeyFile, Path privateKeyFile) throws IOException {
        try {
            byte[] publicBytes = Files.readAllBytes(publicKeyFile);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory publicFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = publicFactory.generatePublic(publicSpec);
    
            byte[] privateBytes = Files.readAllBytes(privateKeyFile);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory privateFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = privateFactory.generatePrivate(privateSpec);
            
            return new EditKeyManager(new KeyPair(publicKey, privateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IOException("Failed to set up KeyPair", e);
        }
    }
}
