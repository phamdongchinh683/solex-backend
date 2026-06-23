package com.example.solex_backend.service;

import com.example.solex_backend.domain.Address;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.CreateAddressRequest;
import com.example.solex_backend.dto.response.AddressResponse;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressResponse> getMyAddresses(User user) {
        return addressRepository.findByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse createAddress(User user, CreateAddressRequest request) {
        List<Address> existing = addressRepository.findByUser(user);
        boolean isDefault = existing.isEmpty();

        Address address = Address.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phoneNumber())
                .addressDetail(request.addressDetail())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .isDefault(isDefault)
                .build();
        addressRepository.save(address);
        return toResponse(address);
    }

    // Rule 1: findByIdAndUser replaces findById + manual ownership comparison
    public AddressResponse updateAddress(Long id, User user, CreateAddressRequest request) {
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));

        address.setFirstName(request.firstName());
        address.setLastName(request.lastName());
        address.setPhone(request.phoneNumber());
        address.setAddressDetail(request.addressDetail());
        address.setLongitude(request.longitude());
        address.setLatitude(request.latitude());
        addressRepository.save(address);
        return toResponse(address);
    }

    // Rule 1: existsByIdAndUser adds the previously missing ownership check before delete
    public void deleteAddress(Long id, User user) {
        if (!addressRepository.existsByIdAndUser(id, user)) {
            throw new ResourceNotFoundException("Address not found: " + id);
        }
        addressRepository.deleteById(id);
    }

    // Rule 1: findByIdAndUser replaces findById + manual check
    // Rule 3: bulk @Modifying queries replace per-row load → set → save loop
    public AddressResponse setDefaultAddress(Long id, User user) {
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));

        addressRepository.clearDefaultByUserId(user.getId());
        addressRepository.setDefaultByIdAndUserId(id, user.getId());
        address.setIsDefault(true);
        return toResponse(address);
    }

    private AddressResponse toResponse(Address address) {
        String fullName = (address.getFirstName() != null ? address.getFirstName() : "")
                + " " + (address.getLastName() != null ? address.getLastName() : "");
        return new AddressResponse(
                address.getId(),
                address.getFirstName(), address.getLastName(), fullName,
                address.getPhone(), address.getLongitude(), address.getLatitude(),
                address.getAddressDetail(), address.getIsDefault(), address.getCreatedAt()
        );
    }
}
