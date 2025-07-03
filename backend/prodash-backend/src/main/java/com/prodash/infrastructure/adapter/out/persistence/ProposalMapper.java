package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.domain.model.Proposal;
import com.prodash.domain.model.Author;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ProposalMapper {

    public ProposalDocument toDocument(Proposal domain) {
        if (domain == null) return null;
        ProposalDocument doc = new ProposalDocument();
        doc.setId(domain.getId());
        doc.setUri(domain.getUri());
        doc.setTitle(domain.getTitle());
        doc.setSiglaTipo(domain.getSiglaTipo());
        doc.setDescricaoTipo(domain.getDescricaoTipo());
        doc.setNumero(domain.getNumero());
        doc.setAno(domain.getAno());
        doc.setSummary(domain.getSummary());
        doc.setEmenta(domain.getEmenta());
        doc.setEmentaDetalhada(domain.getEmentaDetalhada());
        doc.setFullTextUrl(domain.getFullTextUrl());
        doc.setUriAutores(domain.getUriAutores());
        doc.setStatus(domain.getStatus());
        doc.setSituation(domain.getSituation());
        doc.setPresentationDate(domain.getPresentationDate());
        doc.setImpactScore(domain.getImpactScore());
        doc.setJustification(domain.getJustification());
        doc.setDispatch(domain.getDispatch());
        doc.setProcessingAgency(domain.getProcessingAgency());
        if (domain.getAuthors() != null) {
            doc.setAuthors(domain.getAuthors().stream()
                    .map(this::toAuthorDocument)
                    .collect(Collectors.toList()));
        } else {
            doc.setAuthors(Collections.emptyList());
        }
        return doc;
    }

    public Proposal toDomain(ProposalDocument document) {
        if (document == null) return null;
        Proposal domain = new Proposal();
        domain.setId(document.getId());
        domain.setUri(document.getUri());
        domain.setTitle(document.getTitle());
        domain.setSiglaTipo(document.getSiglaTipo());
        domain.setDescricaoTipo(document.getDescricaoTipo());
        domain.setNumero(document.getNumero());
        domain.setAno(document.getAno());
        domain.setSummary(document.getSummary());
        domain.setEmenta(document.getEmenta());
        domain.setEmentaDetalhada(document.getEmentaDetalhada());
        domain.setFullTextUrl(document.getFullTextUrl());
        domain.setUriAutores(document.getUriAutores());
        domain.setStatus(document.getStatus());
        domain.setSituation(document.getSituation());
        domain.setPresentationDate(document.getPresentationDate());
        domain.setImpactScore(document.getImpactScore());
        domain.setJustification(document.getJustification());
        domain.setDispatch(document.getDispatch());
        domain.setProcessingAgency(document.getProcessingAgency());
        if (document.getAuthors() != null) {
            domain.setAuthors(document.getAuthors().stream()
                    .map(this::toAuthorDomain)
                    .collect(Collectors.toList()));
        }
        return domain;
    }

    private AuthorDocument toAuthorDocument(Author author) {
        if (author == null) return null;
        AuthorDocument doc = new AuthorDocument();
        doc.setName(author.getName());
        doc.setType(author.getType());
        return doc;
    }

    private Author toAuthorDomain(AuthorDocument doc) {
        if (doc == null) return null;
        Author author = new Author();
        author.setName(doc.getName());
        author.setType(doc.getType());
        return author;
    }
}