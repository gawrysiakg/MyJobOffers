package com.junioroffers.domain.offer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOfferRepository implements OfferRepository
{


    Map<String, Offer> database = new ConcurrentHashMap<>();

    @Override
    public boolean existsByOfferUrl(String offerUrl) {
        return database.values().stream().anyMatch(offer -> offer.offerUrl().equals(offerUrl));
    }

    @Override
    public Optional<Offer> findByOfferUrl(String offerUrl) {
        return Optional.of(database.get(offerUrl));

    }

    @Override
    public List<Offer> saveAll(List<Offer> offers) {
        List<Offer> list = offers.stream().map(this::save).toList();
        return list;
    }

    @Override
    public List<Offer> findAll() {
        return database.values().stream().toList();
    }

    @Override
    public Optional<Offer> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public Offer save(Offer entity) {
        if (database.values().stream().anyMatch(offer -> offer.offerUrl().equals(entity.offerUrl()))) {
            throw new OfferDuplicateException(entity.offerUrl());
        }
        UUID id = UUID.randomUUID();        //najpierw sprawdzamy czy już nie mamy tego ogłoszenia
        Offer offer = new Offer(            //które chcemy zapisać
                id.toString(),
                entity.companyName(),
                entity.position(),
                entity.salary(),
                entity.offerUrl()
        );
        database.put(id.toString(), offer);
        return offer;
    }


}
