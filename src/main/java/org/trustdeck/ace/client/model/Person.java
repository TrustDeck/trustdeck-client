package org.trustdeck.ace.client.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person {
    private Integer id;
    private String firstName;
    private String lastName;
    private String birthName;
    private String administrativeGender;
    private String dateOfBirth;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private String identifier;
    private String idType;
    private Algorithm algorithm;
}
