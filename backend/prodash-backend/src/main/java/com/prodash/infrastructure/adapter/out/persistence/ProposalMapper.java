package com.prodash.infrastructure.adapter.out.persistence;

import com.prodash.domain.model.Proposal;
import com.prodash.domain.model.Vote;
import com.prodash.domain.model.Voting;
import com.prodash.domain.model.Author;
import com.prodash.domain.model.Deputy;
import com.prodash.domain.model.Party;
import com.prodash.domain.model.Theme;

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
        if (domain.getThemes() != null) {
            doc.setThemes(domain.getThemes().stream()
                .map(this::toThemeDocument)
                .collect(Collectors.toList()));
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
        if (document.getThemes() != null) {
            domain.setThemes(document.getThemes().stream()
                .map(this::toThemeDomain)
                .collect(Collectors.toList()));
        }
        return domain;
    }

    private AuthorDocument toAuthorDocument(Author author) {
        if (author == null) return null;
        AuthorDocument doc = new AuthorDocument();
        doc.setName(author.name());
        doc.setType(author.type()); 
        return doc;
    }
    
    private Author toAuthorDomain(AuthorDocument doc) {
        if (doc == null) return null;
        return new Author(doc.getName(), doc.getType());
    }

    public DeputyDocument toDocument(Deputy domain) {
        if (domain == null) return null;
        DeputyDocument doc = new DeputyDocument();
        doc.setId(domain.id());
        doc.setNomeCivil(domain.nomeCivil());
        doc.setNomeParlamentar(domain.nomeParlamentar());
        doc.setSiglaUf(domain.siglaUf());
        doc.setPartidoId(domain.partidoId());
        return doc;
    }

    public Deputy toDomain(DeputyDocument doc) {
        if (doc == null) return null;
        return new Deputy(doc.getId(), doc.getNomeCivil(), doc.getNomeParlamentar(), doc.getSiglaUf(), doc.getPartidoId());
    }

    // Mapeadores para Party
    public PartyDocument toDocument(Party domain) {
        if (domain == null) return null;
        PartyDocument doc = new PartyDocument();
        doc.setId(domain.id());
        doc.setSigla(domain.sigla());
        doc.setNome(domain.nome());
        return doc;
    }

    public Party toDomain(PartyDocument doc) {
        if (doc == null) return null;
        return new Party(doc.getId(), doc.getSigla(), doc.getNome());
    }

    // Mapeadores para Voting
    public VotingDocument toDocument(Voting domain) {
        if (domain == null) return null;
        VotingDocument doc = new VotingDocument();
        doc.setId(domain.id());
        doc.setProposicaoId(domain.proposicaoId());
        doc.setData(domain.data());
        doc.setResumo(domain.resumo());
        return doc;
    }

    public Voting toDomain(VotingDocument doc) {
        if (doc == null) return null;
        return new Voting(doc.getId(), doc.getProposicaoId(), doc.getData(), doc.getResumo());
    }

    // Mapeadores para Vote
    public VoteDocument toDocument(Vote domain) {
        if (domain == null) return null;
        VoteDocument doc = new VoteDocument();
        doc.setVotacaoId(domain.votacaoId());
        doc.setDeputadoId(domain.deputadoId());
        doc.setTipoVoto(domain.tipoVoto());
        return doc;
    }

    public Vote toDomain(VoteDocument doc) {
        if (doc == null) return null;
        return new Vote(doc.getVotacaoId(), doc.getDeputadoId(), doc.getTipoVoto());
    }

    private ThemeDocument toThemeDocument(Theme theme) {
        if (theme == null) return null;
        ThemeDocument doc = new ThemeDocument();
        doc.setCod(theme.cod());
        doc.setNome(theme.nome());
        return doc;
    }
    
    private Theme toThemeDomain(ThemeDocument doc) {
        if (doc == null) return null;
        return new Theme(doc.getCod(), doc.getNome());
    }
}