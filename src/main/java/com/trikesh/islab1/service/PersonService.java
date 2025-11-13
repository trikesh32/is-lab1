package com.trikesh.islab1.service;

import com.trikesh.islab1.controller.PersonWebSocketController;
import com.trikesh.islab1.model.Color;
import com.trikesh.islab1.model.Person;
import com.trikesh.islab1.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Person save(Person person) {
        boolean isNew = person.getId() == null;
        
        if (isNew) {
            validateUniqueConstraints(person);
        }
        
        Person savedPerson = personRepository.save(person);
        if (isNew) {
            webSocketController.notifyPersonCreated(savedPerson);
        } else {
            webSocketController.notifyPersonUpdated(savedPerson);
        }
        return savedPerson;
    }
    
    private void validateUniqueConstraints(Person person) {
        validateNameCoordinatesBirthday(person);
        validateLocationForSamePhysicalParams(person);
        validateAppearanceNationalityInLocation(person);
        validateNameProximity(person);
    }
    
    private void validateNameCoordinatesBirthday(Person person) {
        List<Person> existingPersons = personRepository.findByNameContainingIgnoreCase(person.getName(), null).getContent();
        for (Person existing : existingPersons) {
            if (existing.getName().equals(person.getName()) &&
                existing.getCoordinates().getX() == person.getCoordinates().getX() &&
                existing.getCoordinates().getY().equals(person.getCoordinates().getY()) &&
                existing.getBirthday().equals(person.getBirthday())) {
                throw new IllegalArgumentException(
                    "Person with same name, coordinates and birthday already exists"
                );
            }
        }
    }
    
    private void validateLocationForSamePhysicalParams(Person person) {
        if (person.getHeight() == null) return;
        
        List<Person> allPersons = personRepository.findAll();
        for (Person existing : allPersons) {
            if (existing.getId() != null && existing.getId().equals(person.getId())) continue;
            
            if (existing.getHeight() != null &&
                existing.getHeight().equals(person.getHeight()) &&
                existing.getWeight().equals(person.getWeight()) &&
                existing.getLocation().getX().equals(person.getLocation().getX()) &&
                existing.getLocation().getY().equals(person.getLocation().getY()) &&
                existing.getLocation().getName().equals(person.getLocation().getName())) {
                throw new IllegalArgumentException(
                    "Person with same height (" + person.getHeight() +
                    "), weight (" + person.getWeight() +
                    ") and location already exists. Two people with identical physical parameters cannot be in the same location"
                );
            }
        }
    }
    
    private void validateAppearanceNationalityInLocation(Person person) {
        List<Person> allPersons = personRepository.findAll();
        for (Person existing : allPersons) {
            if (existing.getId() != null && existing.getId().equals(person.getId())) continue;
            
            if (existing.getEyeColor() == person.getEyeColor() &&
                existing.getHairColor() == person.getHairColor() &&
                existing.getNationality() == person.getNationality() &&
                existing.getLocation().getX().equals(person.getLocation().getX()) &&
                existing.getLocation().getY().equals(person.getLocation().getY()) &&
                existing.getLocation().getName().equals(person.getLocation().getName())) {
                throw new IllegalArgumentException(
                    "Person with same eye color (" + person.getEyeColor() +
                    "), hair color (" + person.getHairColor() +
                    "), nationality (" + person.getNationality() +
                    ") already exists in location '" + person.getLocation().getName() +
                    "'. This combination must be unique per location"
                );
            }
        }
    }
    
    private void validateNameProximity(Person person) {
        List<Person> sameNamePersons = personRepository.findByNameContainingIgnoreCase(person.getName(), null).getContent()
            .stream()
            .filter(p -> p.getName().equals(person.getName()))
            .filter(p -> p.getId() == null || !p.getId().equals(person.getId()))
            .toList();
        
        if (sameNamePersons.isEmpty()) return;
        
        final double RADIUS = 100.0;
        long nearbyCount = sameNamePersons.stream()
            .filter(existing -> {
                double distance = Math.sqrt(
                    Math.pow(existing.getCoordinates().getX() - person.getCoordinates().getX(), 2) +
                    Math.pow(existing.getCoordinates().getY() - person.getCoordinates().getY(), 2)
                );
                return distance <= RADIUS;
            })
            .count();
        
        if (nearbyCount >= 3) {
            throw new IllegalArgumentException(
                "Too many people with name '" + person.getName() +
                "' in proximity (radius " + RADIUS + " units). Maximum 3 people with same name allowed in this area"
            );
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(Long id) {
        if (!personRepository.existsById(id)) {
            throw new IllegalArgumentException("Person with id " + id + " not found");
        }
        personRepository.deleteById(id);
        webSocketController.notifyPersonDeleted(id);
    }

    public boolean existsById(Long id) {
        return personRepository.existsById(id);
    }

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
