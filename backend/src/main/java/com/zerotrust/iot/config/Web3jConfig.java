package com.zerotrust.iot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    private static final Logger log = LoggerFactory.getLogger(Web3jConfig.class);

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.private-key}")
    private String privateKey;

    @Bean
    public Web3j web3j() {
        log.info("Connecting Web3j to RPC endpoint: {}", rpcUrl);
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials deployerCredentials() {
        try {
            return Credentials.create(privateKey);
        } catch (Exception e) {
            log.warn("Failed to initialize Web3j credentials from private key: {}", e.getMessage());
            return null;
        }
    }
}
