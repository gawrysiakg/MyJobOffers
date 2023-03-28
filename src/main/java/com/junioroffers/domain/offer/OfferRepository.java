package com.junioroffers.domain.offer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository //nie trzeba oznaczać jako repository bo MongoRepository juz nim jest ale można informacyjnie oznaczyć
//w mongodb nie mamy encji tylko document
public interface OfferRepository extends MongoRepository<Offer, String> {

    boolean existsByOfferUrl(String offerUrl);

//    Optional<Offer> findByOfferUrl(String offerUrl);

}