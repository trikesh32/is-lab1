package com.trikesh.islab1.controller;

import com.trikesh.islab1.DTO.PersonDTO;
import com.trikesh.islab1.model.Color;
import com.trikesh.islab1.model.Person;
import com.trikesh.islab1.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    @Autowired
    private PersonService personService;

    @GetMapping
    public ResponseEntity<Page<PersonDTO>> getAllPersons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Person> persons = personService.findAll(pageable);
        Page<PersonDTO> personDTOs = persons.map(PersonDTO::new);

        return ResponseEntity.ok(personDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        Optional<Person> person = personService.findById(id);
        return person.map(p -> ResponseEntity.ok(new PersonDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody Person person) {
        Person savedPerson = personService.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PersonDTO(savedPerson));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody Person personDetails) {

        if (!personService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        personDetails.setId(id);
        Person updatedPerson = personService.save(personDetails);
        return ResponseEntity.ok(new PersonDTO(updatedPerson));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        if (!personService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PersonDTO>> searchPersons(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Person> persons = personService.findByNameContaining(name, pageable);
        Page<PersonDTO> personDTOs = persons.map(PersonDTO::new);

        return ResponseEntity.ok(personDTOs);
    }

    @GetMapping("/operations/total-height")
    public ResponseEntity<Long> getTotalHeight() {
        Long totalHeight = personService.calculateTotalHeight();
        return ResponseEntity.ok(totalHeight);
    }

    @GetMapping("/operations/count-by-weight-less-than")
    public ResponseEntity<Long> countByWeightLessThan(@RequestParam Integer weight) {
        Long count = personService.countByWeightLessThan(weight);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/operations/birthday-before")
    public ResponseEntity<List<PersonDTO>> getPersonsWithBirthdayBefore(
            @RequestParam String dateTime) {

        ZonedDateTime birthday = ZonedDateTime.parse(dateTime);
        List<Person> persons = personService.findByBirthdayBefore(birthday);
        List<PersonDTO> personDTOs = persons.stream()
                .map(PersonDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(personDTOs);
    }

    @GetMapping("/operations/hair-color-percentage")
    public ResponseEntity<Double> getHairColorPercentage(@RequestParam Color hairColor) {
        Double percentage = personService.getHairColorPercentage(hairColor);
        return ResponseEntity.ok(percentage);
    }

    @GetMapping("/operations/eye-color-percentage")
    public ResponseEntity<Double> getEyeColorPercentage(@RequestParam Color eyeColor) {
        Double percentage = personService.getEyeColorPercentage(eyeColor);
        return ResponseEntity.ok(percentage);
    }
}