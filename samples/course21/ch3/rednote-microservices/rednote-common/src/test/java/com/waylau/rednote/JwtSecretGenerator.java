package com.waylau.rednote;

import io.jsonwebtoken.security.Keys;

import java.util.Base64;

/**
 * JwtSecretGenerator 生成Base64密钥
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/08
 **/
public class JwtSecretGenerator {
    public static void main(String[] args) {
        String secret = Base64.getEncoder().encodeToString(
                Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded()
        );
        System.out.println(secret);
    }
}
