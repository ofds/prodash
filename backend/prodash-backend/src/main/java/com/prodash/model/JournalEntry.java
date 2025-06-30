// Create this file in the existing package: com.prodash.model
package com.prodash.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "journal_entries")
public class JournalEntry {

    @Id
    private String id;

    @Indexed(unique = true) // Ensure only one entry per day
    private LocalDate date;

    private String title;
    private String narrativeSummary; // The AI-generated journal text
    private List<Long> relatedProposalIds; // List of proposal IDs included in this journal
}