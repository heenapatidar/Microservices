package com.eazybytes.accounts.service.impl;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.IAccountsRepository;
import com.eazybytes.accounts.repository.ICustomerRepository;
import com.eazybytes.accounts.service.IAccountsService;

@Service
public class AccountsServiceImpl implements IAccountsService
{
	@Autowired
	private IAccountsRepository accountsRepository;
	@Autowired
	private ICustomerRepository customerRepository;

	@Override
	public void createAccount(CustomerDto customerDto) 
	{
		Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
		Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
		if(optionalCustomer.isPresent()) 
		{
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                    +customerDto.getMobileNumber());
        }
		//customer.setCreatedAt(LocalDateTime.now());
		//customer.setCreatedBy("Anonymous");
		Customer savedCustomer = customerRepository.save(customer);
		accountsRepository.save(createNewAccount(savedCustomer));
		
	}

	private Accounts createNewAccount(Customer customer) 
	{
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        //newAccount.setCreatedAt(LocalDateTime.now());
        //newAccount.setCreatedBy("Anonymous");
        return newAccount;
    }

	@Override
	public CustomerDto fetchAccount(@RequestParam String mobileNumber) 
	{
		Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
				()->new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
		);
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
				()->new ResourceNotFoundException("Customer","customerId",customer.getCustomerId().toString())
		);
		CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
	}

	@Override
	public boolean updateAccount(CustomerDto customerDto) 
	{
		boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto !=null ){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return  isUpdated;
	}

	@Override
	public boolean deleteAccount(@RequestParam String mobileNumber) 
	{
		Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
	}
}
