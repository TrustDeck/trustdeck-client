package org.trustdeck.ace.client.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Algorithm {

    private Integer id;
    private String name;
    private String alphabet;
    private long randomAlgorithmDesiredSize;
    private double randomAlgorithmDesiredSuccessProbability;
    private long consecutiveValueCounter;
    private int pseudonymLength;
    private String paddingCharacter;
    private boolean addCheckDigit;
    private boolean lengthIncludesCheckDigit;
    private String salt;
    private int saltLength;
}
