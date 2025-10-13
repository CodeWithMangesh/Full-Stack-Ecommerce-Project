package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);
        List<Address> addresses = user.getAddresses();
        addresses.add(address);
        user.setAddresses(addresses);

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getAddress() {
        List<Address> addressList = addressRepository.findAll();
        List<AddressDTO> addressDTOList = addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getUserAddress(User user) {
        //List<Address> addressList = addressRepository.findAddressByUserId(user.getUserId());
        List<Address> addressList = user.getAddresses();
        if(addressList.isEmpty()){
            throw new APIException("No user address found");
        }
        List<AddressDTO> addressDTOList = addressList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
        return addressDTOList;
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address databaseAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        databaseAddress.setBuildingName(addressDTO.getBuildingName() != null ? addressDTO.getBuildingName() : databaseAddress.getBuildingName());
        databaseAddress.setCity(addressDTO.getCity() != null ? addressDTO.getCity() : databaseAddress.getCity());
        databaseAddress.setStreet(addressDTO.getStreet() != null ? addressDTO.getStreet() : databaseAddress.getStreet());
        databaseAddress.setState(addressDTO.getState() != null ? addressDTO.getState() : databaseAddress.getState());
        databaseAddress.setPincode(addressDTO.getPincode() != null ? addressDTO.getPincode() : databaseAddress.getPincode());
        databaseAddress.setCountry(addressDTO.getCountry() != null ? addressDTO.getCountry() : databaseAddress.getCountry());

        Address updatedAddress = addressRepository.save(databaseAddress);

        User user = databaseAddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        AddressDTO updatedAddressDTO = modelMapper.map(updatedAddress, AddressDTO.class);

        return updatedAddressDTO;
    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        User user  = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(addressFromDatabase);
        return "Address deleted successfully with Address Id: " + addressId;
    }
}
