package org.mongo.labs;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    String name;
    Author author;
    Integer pages;
    List<String> reviews;


}
