package com.example.solex_backend.service.notification;

import com.example.solex_backend.config.EsmsConfig;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class EsmsSmsAdapter implements SmsPort {

    private static final String ESMS_URL = "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_get";

    private final RestClient restClient;
    private final EsmsConfig esmsConfig;

    public EsmsSmsAdapter(EsmsConfig esmsConfig) {
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
                + "&SmsType=8"
                + "&IsUnicode=1";

        restClient.get()
                .uri(url)
                .headers(headers -> headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .retrieve()
                .toBodilessEntity();
    }
}