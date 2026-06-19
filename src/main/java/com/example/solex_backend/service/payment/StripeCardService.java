package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.response.CardResponse;
import com.example.solex_backend.dto.response.SetupIntentResponse;
import com.example.solex_backend.exception.BusinessException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeCardService {

    private final StripeAccountService stripeAccountService;

    public SetupIntentResponse createSetupIntent(User user) {
        String customerId = stripeAccountService.ensureStripeCustomer(user);
        try {
            SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                    .setCustomer(customerId)
                    .addPaymentMethodType("card")
                    .build();
            SetupIntent intent = SetupIntent.create(params);
            return new SetupIntentResponse(intent.getClientSecret());
        } catch (StripeException e) {
            throw new BusinessException("Failed to create setup intent: " + e.getMessage());
        }
    }

    public List<CardResponse> listCards(User user) {
        String customerId = stripeAccountService.ensureStripeCustomer(user);
        try {
            Customer customer = Customer.retrieve(customerId);
            String defaultPmId = customer.getInvoiceSettings() != null
                    ? customer.getInvoiceSettings().getDefaultPaymentMethod()
                    : null;

            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();
            PaymentMethodCollection collection = PaymentMethod.list(params);
            return collection.getData().stream()
                    .map(pm -> toCardResponse(pm, defaultPmId))
                    .toList();
        } catch (StripeException e) {
            throw new BusinessException("Failed to list cards: " + e.getMessage());
        }
    }

    public void removeCard(User user, String paymentMethodId) {
        String customerId = stripeAccountService.ensureStripeCustomer(user);
        try {
            PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
            if (!customerId.equals(pm.getCustomer())) {
                throw new BusinessException("Card does not belong to this customer");
            }
            pm.detach();
        } catch (StripeException e) {
            throw new BusinessException("Failed to remove card: " + e.getMessage());
        }
    }

    public void setDefaultCard(User user, String paymentMethodId) {
        String customerId = stripeAccountService.ensureStripeCustomer(user);
        try {
            PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
            if (!customerId.equals(pm.getCustomer())) {
                throw new BusinessException("Card does not belong to this customer");
            }
            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(paymentMethodId)
                                    .build()
                    )
                    .build();
            Customer.retrieve(customerId).update(params);
        } catch (StripeException e) {
            throw new BusinessException("Failed to set default card: " + e.getMessage());
        }
    }

    private CardResponse toCardResponse(PaymentMethod pm, String defaultPmId) {
        PaymentMethod.Card card = pm.getCard();
        return new CardResponse(
                pm.getId(),
                card.getBrand(),
                card.getLast4(),
                card.getExpMonth(),
                card.getExpYear(),
                pm.getId().equals(defaultPmId)
        );
    }
}
