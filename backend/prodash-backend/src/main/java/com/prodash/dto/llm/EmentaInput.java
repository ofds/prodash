// Create this file in a new package: com.prodash.dto.llm
package com.prodash.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Represents the structure of an ementa we send to the LLM
@Data
@AllArgsConstructor
public class EmentaInput {
    private Long id;
    private String ementa;
}
