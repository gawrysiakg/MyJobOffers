package com.junioroffers.domain.offer;

import com.junioroffers.domain.offer.dto.OfferRequestDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class OfferFacade {

    private final OfferRepository offerRepository;
    private final OfferService offerService;

    public List<OfferResponseDto> findAllOffers() {
        return offerRepository.findAll()
                .stream()
//                .map(offer-> new OfferResponseDto(offer.id(), offer.companyName(), offer.position(), offer.salary(), offer.offerUrl()))
//                .collect(Collectors.toList());
                .map(OfferMapper::mapFromOfferToOfferDto)
                .collect(Collectors.toList());

    }

    public List<OfferResponseDto> fetchAllOffersAndSaveAllIfNotExists() {
            return offerService.fetchAllOffersAndSaveAllIfNotExists()
                    .stream()
                    .map(OfferMapper::mapFromOfferToOfferDto)
                    .collect(Collectors.toList());
    }

    public OfferResponseDto findOfferById(String id) {
            return offerRepository.findById(id)
                    .map(OfferMapper::mapFromOfferToOfferDto)
                    .orElseThrow(()->new OfferNotFoundException(id));
    }

    public OfferResponseDto saveOffer(OfferRequestDto offerDto) {
           Offer offer = offerRepository.save(OfferMapper.mapFromOfferDtoToOffer(offerDto));
            return OfferMapper.mapFromOfferToOfferDto(offer);
    }

    }