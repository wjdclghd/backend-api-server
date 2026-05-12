package com.jch.backendapi.global.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    @Valid
    private EndpointPolicy signup = new EndpointPolicy();

    @Valid
    private EndpointPolicy login = new EndpointPolicy();

    @Valid
    private EndpointPolicy refresh = new EndpointPolicy();

    @Valid
    private EndpointPolicy logout = new EndpointPolicy();

    public EndpointPolicy signup() {
        return signup;
    }

    public void setSignup(EndpointPolicy signup) {
        this.signup = signup;
    }

    public EndpointPolicy login() {
        return login;
    }

    public void setLogin(EndpointPolicy login) {
        this.login = login;
    }

    public EndpointPolicy refresh() {
        return refresh;
    }

    public void setRefresh(EndpointPolicy refresh) {
        this.refresh = refresh;
    }

    public EndpointPolicy logout() {
        return logout;
    }

    public void setLogout(EndpointPolicy logout) {
        this.logout = logout;
    }

    public static class EndpointPolicy {

        @Positive
        private int ipCapacity = 10;

        @Positive
        private long ipWindowSeconds = 60;

        @Positive
        private int keyCapacity = 3;

        @Positive
        private long keyWindowSeconds = 3600;

        public int ipCapacity() {
            return ipCapacity;
        }

        public void setIpCapacity(int ipCapacity) {
            this.ipCapacity = ipCapacity;
        }

        public long ipWindowSeconds() {
            return ipWindowSeconds;
        }

        public void setIpWindowSeconds(long ipWindowSeconds) {
            this.ipWindowSeconds = ipWindowSeconds;
        }

        public int keyCapacity() {
            return keyCapacity;
        }

        public void setKeyCapacity(int keyCapacity) {
            this.keyCapacity = keyCapacity;
        }

        public long keyWindowSeconds() {
            return keyWindowSeconds;
        }

        public void setKeyWindowSeconds(long keyWindowSeconds) {
            this.keyWindowSeconds = keyWindowSeconds;
        }
    }
}
