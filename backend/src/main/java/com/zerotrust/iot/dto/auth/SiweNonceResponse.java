package com.zerotrust.iot.dto.auth;

public class SiweNonceResponse {
    private String nonce;
    private String message;
    private String domain;
    private String uri;
    private String chainId;

    public SiweNonceResponse() {}

    public SiweNonceResponse(String nonce, String message, String domain, String uri, String chainId) {
        this.nonce = nonce;
        this.message = message;
        this.domain = domain;
        this.uri = uri;
        this.chainId = chainId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nonce;
        private String message;
        private String domain;
        private String uri;
        private String chainId;

        public Builder nonce(String nonce) { this.nonce = nonce; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder domain(String domain) { this.domain = domain; return this; }
        public Builder uri(String uri) { this.uri = uri; return this; }
        public Builder chainId(String chainId) { this.chainId = chainId; return this; }

        public SiweNonceResponse build() {
            return new SiweNonceResponse(nonce, message, domain, uri, chainId);
        }
    }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
}
