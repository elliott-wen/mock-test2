package nz.ac.auckland.se325.diary.model;

import java.time.LocalDate;

public record Entry(Long id, LocalDate date, String title, String body) {
}
