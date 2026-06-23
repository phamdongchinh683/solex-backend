package com.example.solex_backend.service.payment;

import com.example.solex_backend.config.VNPayConfig;
import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.Payment;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.util.Enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class VNPayPaymentStrategy implements PaymentStrategy {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayConfig vnPayConfig;

    @Override
    public PaymentInitResult initiate(Order order, Payment payment, String clientIp) {
        LocalDateTime now = LocalDateTime.now();

        long vnpAmount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CreateDate", now.format(DATE_FORMAT));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getTransactionRef());
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_TxnRef", payment.getTransactionRef());
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DATE_FORMAT));

        String queryString = buildQueryString(params);
        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), queryString);
        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        return new PaymentInitResult(payment.getTransactionRef(), null, paymentUrl);
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.VNPAY;
    }

    public boolean verifySignature(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null) return false;

        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        String queryString = buildQueryString(signParams);
        String computed = hmacSHA512(vnPayConfig.getHashSecret(), queryString);
        return received.equalsIgnoreCase(computed);
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!sb.isEmpty()) sb.append('&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII))
              .append('=')
              .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new BusinessException("Không thể tạo mã HMAC-SHA512");
        }
    }
}
