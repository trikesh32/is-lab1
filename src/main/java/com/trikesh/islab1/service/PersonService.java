package com.trikesh.islab1.service;

import com.trikesh.islab1.controller.PersonWebSocketController;
import com.trikesh.islab1.model.Color;
import com.trikesh.islab1.model.Person;
import com.trikesh.islab1.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonWebSocketController webSocketController;

    public Page<Person> findAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    public Page<Person> findByNameContaining(String name, Pageable pageable) {
        return personRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public Person save(Person person) {
        boolean isNew = person.getId() == null;
        Person savedPerson = personRepository.save(person);
        if (isNew) {
            webSocketController.notifyPersonCreated(savedPerson);
        } else {
            webSocketController.notifyPersonUpdated(savedPerson);
        }
        return savedPerson;
    }

    public void deleteById(Long id) {
        personRepository.deleteById(id);
        webSocketController.notifyPersonDeleted(id);
    }

    public boolean existsById(Long id) {
        return personRepository.existsById(id);
    }

    // Специальные операции
    public Long calculateTotalHeight() {
        Long sum = personRepository.sumHeight();
        return sum != null ? sum : 0L;
    }

    public Long countByWeightLessThan(Integer weight) {
        return personRepository.countByWeightLessThan(weight);
    }

    public List<Person> findByBirthdayBefore(ZonedDateTime birthday) {
        return personRepository.findByBirthdayBefore(birthday);
    }

    public Double getHairColorPercentage(Color hairColor) {
        Long countWithColor = personRepository.countByHairColor(hairColor);
        Long totalCount = personRepository.countAll();

        if (totalCount == 0) return 0.0;
        return (countWithColor.doubleValue() / totalCount.doubleValue()) * 100.0;
    }

    public Double getEyeColorPercentage(Color eyeColor) {
        Long countWithColor = personRepository.countByEyeColor(eyeColor);
        Long totalCount = personRepository.countAll();

        if (totalCount == 0) return 0.0;
        return (countWithColor.doubleValue() / totalCount.doubleValue()) * 100.0;
    }
}
