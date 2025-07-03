package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.domain.model.Proposal;
import org.springframework.stereotype.Component;

@Component
public class ProposalMapper {

    public ProposalDocument toDocument(Proposal domain) {
        ProposalDocument doc = new ProposalDocument();
        doc.setId(domain.getId());
        doc.setTitle(domain.getTitle());
        doc.setSummary(domain.getSummary());
        doc.setEmenta(domain.getEmenta());
        doc.setFullTextUrl(domain.getFullTextUrl());
        doc.setStatus(domain.getStatus());
        doc.setPresentationDate(domain.getPresentationDate());
        doc.setImpactScore(domain.getImpactScore());
        doc.setJustification(domain.getJustification());
        return doc;
    }

    public Proposal toDomain(ProposalDocument document) {
        Proposal domain = new Proposal();
        domain.setId(document.getId());
        domain.setTitle(document.getTitle());
        domain.setSummary(document.getSummary());
        domain.setEmenta(document.getEmenta());
        domain.setFullTextUrl(document.getFullTextUrl());
        domain.setStatus(document.getStatus());
        domain.setPresentationDate(document.getPresentationDate());
        domain.setImpactScore(document.getImpactScore());
        domain.setJustification(document.getJustification());
        return domain;
    }
}