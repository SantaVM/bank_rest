package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CryptoUtils {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    @Value("${app.crypto.key}")
    private String secret_key;
    @Value("${app.crypto.vector}")
    private String init_vector;

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(init_vector.getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(secret_key.getBytes(),
                    "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("Error encrypting", ex);
        }
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(init_vector.getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(secret_key.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            throw new RuntimeException("Error decrypting", ex);
        }
    }
}