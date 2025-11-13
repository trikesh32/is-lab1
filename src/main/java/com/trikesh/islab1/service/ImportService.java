package com.trikesh.islab1.service;

import com.trikesh.islab1.controller.PersonWebSocketController;
import com.trikesh.islab1.model.*;
import com.trikesh.islab1.repository.ImportHistoryRepository;
import com.trikesh.islab1.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ImportService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ImportHistoryRepository importHistoryRepository;

    @Autowired
    private PersonWebSocketController webSocketController;

    private void validateUniqueConstraints(Person person, Set<String> importedKeys) {
        String uniqueKey = generateUniqueKey(person);
        
        if (importedKeys.contains(uniqueKey)) {
            throw new IllegalArgumentException(
                "Duplicate person in import file: " + person.getName() + 
                " with coordinates (" + person.getCoordinates().getX() + ", " + 
                person.getCoordinates().getY() + ")"
            );
        }
        
        List<Person> existingPersons = personRepository.findByNameContainingIgnoreCase(person.getName(), null).getContent();
        for (Person existing : existingPersons) {
            if (generateUniqueKey(existing).equals(uniqueKey)) {
                throw new IllegalArgumentException(
                    "Person already exists in database: " + person.getName() + 
                    " with same coordinates and birthday"
                );
            }
        }
        
        importedKeys.add(uniqueKey);
    }

    private String generateUniqueKey(Person person) {
        return person.getName() + "|" + 
               person.getCoordinates().getX() + "|" + 
               person.getCoordinates().getY() + "|" + 
               person.getBirthday().toString();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public ImportHistory importPersonsFromCsv(MultipartFile file, User user) {
        ImportHistory history = new ImportHistory(user, file.getOriginalFilename());
        history = importHistoryRepository.save(history);

        try {
            List<Person> persons = parseCsvFile(file);
            
            if (persons.isEmpty()) {
                throw new IllegalArgumentException("No valid persons found in file");
            }

            List<Person> savedPersons = personRepository.saveAll(persons);

            history.setStatus(ImportStatus.SUCCESS);
            history.setObjectsCount(savedPersons.size());
            history.setCompletedAt(LocalDateTime.now());
            importHistoryRepository.save(history);

            for (Person person : savedPersons) {
                webSocketController.notifyPersonCreated(person);
            }

            log.info("Successfully imported {} persons from file {}", savedPersons.size(), file.getOriginalFilename());
            return history;

        } catch (Exception e) {
            log.error("Error importing persons from file {}: {}", file.getOriginalFilename(), e.getMessage());
            history.setStatus(ImportStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            history.setCompletedAt(LocalDateTime.now());
            importHistoryRepository.save(history);
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    private List<Person> parseCsvFile(MultipartFile file) throws Exception {
        List<Person> persons = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            reader.readLine();
            lineNumber++;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Person person = parseCsvLine(line);
                    validateUniqueConstraints(person, uniqueKeys);
                    persons.add(person);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        "Error parsing line " + lineNumber + ": " + e.getMessage(), e
                    );
                }
            }
        }

        return persons;
    }

    private Person parseCsvLine(String line) {
        String[] fields = line.split(",");
        
        if (fields.length < 12) {
            throw new IllegalArgumentException("Invalid CSV format: expected 12 fields, got " + fields.length);
        }

        Person person = new Person();
        
        person.setName(fields[0].trim());

        Coordinates coordinates = new Coordinates();
        coordinates.setX(Double.parseDouble(fields[1].trim()));
        coordinates.setY(Float.parseFloat(fields[2].trim()));
        person.setCoordinates(coordinates);
        
        person.setEyeColor(Color.valueOf(fields[3].trim().toUpperCase()));
        person.setHairColor(Color.valueOf(fields[4].trim().toUpperCase()));
        
        Location location = new Location();
        location.setX(Float.parseFloat(fields[5].trim()));
        location.setY(Integer.parseInt(fields[6].trim()));
        location.setName(fields[7].trim());
        person.setLocation(location);
        
        String heightStr = fields[8].trim();
        if (!heightStr.isEmpty()) {
            person.setHeight(Long.parseLong(heightStr));
        }
        
        person.setBirthday(ZonedDateTime.parse(fields[9].trim(), DateTimeFormatter.ISO_ZONED_DATE_TIME));

        person.setWeight(Integer.parseInt(fields[10].trim()));

        person.setNationality(Country.valueOf(fields[11].trim().toUpperCase()));

        return person;
    }
}