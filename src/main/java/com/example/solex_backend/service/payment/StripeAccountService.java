package com.example.solex_backend.service.payment;

import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.response.StripeConnectResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.RestaurantRepository;
import com.example.solex_backend.repository.UserRepository;
import com.example.solex_backend.dto.response.StripeBalanceResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Balance;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeAccountService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Value("${stripe.connect.return-url:http://localhost:3000/operator/stripe/return}")
    private String connectReturnUrl;

    @Value("${stripe.connect.refresh-url:http://localhost:3000/operator/stripe/refresh}")
    private String connectRefreshUrl;

    @Transactional
    public String ensureStripeCustomer(User user) {
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getFirstName() + " " + user.getLastName())
                    .putMetadata("userId", user.getId().toString())
                    .build();
            Customer customer = Customer.create(params);
            log.info("Stripe customer created: id={}", customer.getId());
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
            return customer.getId();
        } catch (StripeException e) {
            throw new BusinessException("Failed to create Stripe customer: " + e.getMessage());
        }
    }

    @Transactional
    public StripeConnectResponse createConnectAccount(User operator) {
        Restaurant restaurant = restaurantRepository.findByOperator(operator)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for this operator"));
        try {
            String accountId = restaurant.getStripeAccountId();
            if (accountId == null) {
                AccountCreateParams params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setEmail(operator.getEmail())
                        .putMetadata("operatorId", operator.getId().toString())
                        .putMetadata("restaurantId", restaurant.getId().toString())
                        .build();
                Account account = Account.create(params);
                accountId = account.getId();
                log.info("Stripe Connect account created: id={}", accountId);
                restaurant.setStripeAccountId(accountId);
                restaurantRepository.save(restaurant);
            }
            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setReturnUrl(connectReturnUrl)
                    .setRefreshUrl(connectRefreshUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();
            AccountLink link = AccountLink.create(linkParams);
            return new StripeConnectResponse(accountId, link.getUrl());
        } catch (StripeException e) {
            throw new BusinessException("Failed to create Stripe Connect account: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public StripeBalanceResponse getConnectedBalance(User operator) {
        Restaurant restaurant = restaurantRepository.findByOperator(operator)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for this operator"));
        String stripeAccountId = restaurant.getStripeAccountId();
        if (stripeAccountId == null) {
            throw new BusinessException("Stripe account not connected. Complete onboarding first.");
        }
        try {
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(stripeAccountId)
                    .build();
            Balance balance = Balance.retrieve(options);
            return new StripeBalanceResponse(
                    balance.getAvailable().get(0).getAmount(),
                    balance.getPending().get(0).getAmount(),
                    balance.getAvailable().get(0).getCurrency()
            );
        } catch (StripeException e) {
            throw new BusinessException("Failed to retrieve Stripe balance: " + e.getMessage());
        }
    }
}
