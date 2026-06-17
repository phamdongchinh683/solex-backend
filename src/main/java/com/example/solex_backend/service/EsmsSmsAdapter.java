package com.example.solex_backend.service;

import com.example.solex_backend.config.EsmsSmsConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EsmsSmsAdapter implements SmsPort {

    private static final String ESMS_URL = "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_get";

    private final RestClient restClient;
    private final EsmsSmsConfig esmsConfig;

    public EsmsSmsAdapter(EsmsSmsConfig esmsConfig) {
        this.esmsConfig = esmsConfig;
        this.restClient = RestClient.create();
    }

    @Override
    public void sendOtp(String to, String otp) {
        String content = "OTP Solex: " + otp;
        String url = ESMS_URL
                + "?ApiKey=" + esmsConfig.getApiKey()
                + "&SecretKey=" + esmsConfig.getSecret()
                + "&Phone=" + to
                + "&Content=" + org.springframework.web.util.UriUtils.encode(content, java.nio.charset.StandardCharsets.UTF_8)
                + "&Brandname=Solex"
                + "&SmsType=2"
                + "&IsUnicode=0";

        restClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity();
    }
}