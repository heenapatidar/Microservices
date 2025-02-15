package com.eazybytes.cards.service.impl;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.eazybytes.cards.constants.CardsConstants;
import com.eazybytes.cards.dto.CardsDto;
import com.eazybytes.cards.entity.Cards;
import com.eazybytes.cards.exception.CardAlreadyExistsException;
import com.eazybytes.cards.exception.ResourceNotFoundException;
import com.eazybytes.cards.mapper.CardsMapper;
import com.eazybytes.cards.repository.ICardsRepository;
import com.eazybytes.cards.service.ICardsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CardsServiceImpl implements ICardsService
{

	private ICardsRepository iCardsRepository;
	
	@Override
	public void createCard(String mobileNumber)
	{
		Optional<Cards> optionalCards= iCardsRepository.findByMobileNumber(mobileNumber);
        if(optionalCards.isPresent()){
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber "+mobileNumber);
        }
        iCardsRepository.save(createNewCard(mobileNumber));		
	}
	
	/**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new card details
     */
    private Cards createNewCard(String mobileNumber) 
    {
        Cards newCard = new Cards();
        long randomCardNumber = 100000000000L + new Random().nextInt(900000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));
        newCard.setMobileNumber(mobileNumber);
        newCard.setCardType(CardsConstants.CREDIT_CARD);
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        return newCard;
    }

    @Override
    public CardsDto fetchCard(String mobileNumber) 
    {
        Cards cards = iCardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        return CardsMapper.mapToCardsDto(cards, new CardsDto());
    }

    /**
    *
    * @param cardsDto - CardsDto Object
    * @return boolean indicating if the update of card details is successful or not
    */
   @Override
   public boolean updateCard(CardsDto cardsDto) 
   {
       Cards cards = iCardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
               () -> new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber()));
       CardsMapper.mapToCards(cardsDto, cards);
       iCardsRepository.save(cards);
       return  true;
   }

   /**
    * @param mobileNumber - Input MobileNumber
    * @return boolean indicating if the delete of card details is successful or not
    */
   @Override
   public boolean deleteCard(String mobileNumber) 
   {
       Cards cards = iCardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
               () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
       );
       iCardsRepository.deleteById(cards.getCardId());
       return true;
   }

}
