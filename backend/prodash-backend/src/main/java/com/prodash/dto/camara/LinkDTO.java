package com.prodash.dto.camara;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// DTO for the 'links' part of the paginated response, used for navigation
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkDTO {
    private String rel; // e.g., "self", "next", "first", "last"
    private String href;
}