Of course. Here is the plan in a standard markdown format that you can use for your GitHub README.

# ProposalAI: Refactoring to Clean Architecture

This document outlines the plan and tracks the progress of refactoring the ProposalAI application to a clean, modular architecture. The goal is to improve maintainability, testability, and overall code quality.

**Last Updated:** July 1, 2025

-----

## 🎯 1. Project Goals & Guiding Principles

### Goals

  - **Improve Maintainability**: Make the system easier to understand, modify, and extend.
  - **Enhance Testability**: Isolate business logic to allow for robust unit testing without external dependencies.
  - **Increase Modularity**: Decouple components so that changes in one part of the system have minimal impact on others.
  - **Preserve Functionality**: Ensure all existing features of the application continue to work as expected.

### Guiding Principles

  - **Clean Architecture**: We will adhere to the principles of Clean Architecture, separating the code into distinct layers (Domain, Application, Infrastructure). The core rule is the **Dependency Rule**: source code dependencies can only point inwards. Nothing in an inner layer can know anything at all about something in an outer layer.
  - **SOLID**: The new design will follow SOLID principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion).
  - **Domain-Driven Design (DDD) Lite**: We will focus on a rich, framework-independent domain model at the core of our application.

-----

## 🏗️ 2. Architecture Blueprint: Target Folder Structure

This is the target folder structure for the refactored application.

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── example/
    │           └── proposalai/
    │               ├── application/
    │               │   ├── port/
    │               │   │   ├── in/
    │               │   │   │   ├── IngestProposalsUseCase.java
    │               │   │   │   └── ScoreProposalsUseCase.java
    │               │   │   └── out/
    │               │   │       ├── CamaraApiPort.java
    │               │   │       ├── LlmPort.java
    │               │   │       └── ProposalRepositoryPort.java
    │               │   └── service/
    │               │       ├── ProposalIngestionService.java
    │               │       └── ProposalScoringService.java
    │               ├── domain/
    │               │   ├── model/
    │               │   │   └── Proposal.java
    │               │   └── service/
    │               │       └── ProposalValidator.java
    │               └── infrastructure/
    │                   ├── adapter/
    │                   │   ├── in/
    │                   │   │   ├── scheduler/
    │                   │   │   │   └── ProposalSyncScheduler.java
    │                   │   │   └── web/
    │                   │   │       └── ProposalController.java
    │                   │   └── out/
    │                   │       ├── camara/
    │                   │       │   ├── CamaraApiAdapter.java
    │                   │       │   └── dto/
    │                   │       │       └── CamaraProposalDTO.java
    │                   │       ├── llm/
    │                   │       │   ├── LlmAdapter.java
    │                   │       │   └── dto/
    │                   │       │       ├── LlmRequest.java
    │                   │       │       └── LlmResponse.java
    │                   │       └── persistence/
    │                   │           ├── ProposalMongoRepository.java
    │                   │           └── ProposalDocument.java
    │                   ├── config/
    │                   │   ├── AppConfig.java
    │                   │   └── MongoConfig.java
    └── resources/
        ├── prompts/
        │   ├── impact_score_prompt.txt
        │   └── summarize_proposals_prompt.txt
        └── application.yml
```

-----

## 🗺️ 3. Refactoring Roadmap & Task Checklist

This roadmap is broken down into phases. Each task has a checkbox to track its status.

### Phase 1: Project Setup & Domain Layer

The goal of this phase is to establish the core business logic and entities of the application, completely independent of any frameworks or external concerns.

| Status | Task | Description |
| :--- | :--- | :--- |
| [ ] | **Setup New Project Structure** | Create the new folder structure as defined in the architecture blueprint. |
| [ ] | **Define `Proposal` Domain Model** | Create `domain/model/Proposal.java`. This should be a POJO representing the core entity. |
| [ ] | **Implement `ProposalValidator` Service** | Create `domain/service/ProposalValidator.java` with core validation logic. |
| [ ] | **Write Unit Tests for Domain** | Create unit tests for the `Proposal` model and `ProposalValidator` to ensure business rules are correct. |

**✅ Definition of Done for Phase 1:** The `domain` package is complete and fully unit-tested. It has zero dependencies on Spring, MongoDB, or any other external library.

-----

### Phase 2: Application Layer (Use Cases & Ports)

This phase defines the application's specific use cases and the interfaces (ports) that connect the application to the outside world.

| Status | Task | Description |
| :--- | :--- | :--- |
| [ ] | **Define Input Ports (Use Cases)** | Create the `IngestProposalsUseCase.java` and `ScoreProposalsUseCase.java` interfaces. |
| [ ] | **Define Output Ports (Interfaces)** | Create the `CamaraApiPort.java`, `LlmPort.java`, and `ProposalRepositoryPort.java` interfaces. |
| [ ] | **Implement `ProposalIngestionService`** | Create the service that implements `IngestProposalsUseCase`. It will depend on the output ports. |
| [ ] | **Implement `ProposalScoringService`** | Create the service that implements `ScoreProposalsUseCase`. |
| [ ] | **Write Unit Tests for Application Services** | Write unit tests for the application services, using mocks for the output ports. |

**✅ Definition of Done for Phase 2:** The `application` package is complete. All use cases are defined and implemented, and they are tested in isolation from the infrastructure layer.

-----

### Phase 3: Infrastructure Layer (Adapters & Configuration)

This phase implements the details. Here we connect our application to the real world using frameworks and external libraries.

| Status | Task | Description |
| :--- | :--- | :--- |
| [ ] | **Implement `CamaraApiAdapter`** | Create the client for the Câmara API, implementing the `CamaraApiPort`. |
| [ ] | **Implement `LlmAdapter`** | Create the client that communicates with the external LLM API, implementing the `LlmPort`. |
| [ ] | **Implement `ProposalMongoRepository` Adapter** | Create the MongoDB implementation of the `ProposalRepositoryPort`. |
| [ ] | **Create Data Transfer Objects (DTOs)** | Define DTOs for Camara, LLM, and the `ProposalDocument` for MongoDB persistence. |
| [ ] | **Implement `ProposalController`** | Create the Spring REST controller that calls the application use cases. |
| [ ] | **Implement `ProposalSyncScheduler`** | Create the scheduled task to trigger data ingestion and scoring. |
| [ ] | **Configure Spring Beans** | Set up `AppConfig.java` and `MongoConfig.java` to wire all the components together. |
| [ ] | **Migrate `application.properties` to `application.yml`** | Convert and update the configuration file. |

**✅ Definition of Done for Phase 3:** All ports defined in the application layer have concrete implementations in the infrastructure layer. The application is runnable.

-----

### Phase 4: Integration, Testing & Finalization

The final phase is to ensure all the layers work together correctly and to clean up the old code.

| Status | Task | Description |
| :--- | :--- | :--- |
| [ ] | **Write Integration Tests** | Create integration tests that verify the flow from the controller/scheduler down to the database. |
| [ ] | **Perform Manual End-to-End Testing** | Manually test the API endpoints and verify the data flow and storage. |
| [ ] | **Review and Refactor** | Review the new code for any improvements or inconsistencies. |
| [ ] | **Remove Old Code** | Once the new implementation is verified, safely remove the old classes and packages. |
| [ ] | **Update Documentation** | Update the project's `README.md` to reflect the new architecture. |

**✅ Definition of Done for Phase 4:** The new application is fully tested, functional, and has replaced the old implementation.

-----

## 🔄 4. Code Migration Guide

Use this table to map old classes to their new counterparts.

| Old Class (`com.prodash.*`) | New Component(s) | Responsibility in New Architecture | Status |
| :--- | :--- | :--- | :--- |
| `model/Proposal.java` | `domain/model/Proposal.java`, `infrastructure/adapter/out/persistence/ProposalDocument.java` | The core logic is in `domain`, persistence details are in `infrastructure`. | [ ] |
| `service/CamaraApiService.java` | `infrastructure/adapter/out/camara/CamaraApiAdapter.java` | An output adapter responsible for fetching data from the Câmara API. Called by `ProposalIngestionService`. | [ ] |
| `service/LlmService.java` | `infrastructure/adapter/out/llm/LlmAdapter.java` | The concrete implementation of the `LlmPort`. | [ ] |
| `service/ProposalService.java` | `application/service/ProposalIngestionService.java`, `application/service/ProposalScoringService.java` | Responsibilities are now split into specific use cases. | [ ] |
| `repository/ProposalRepository.java` | `application/port/out/ProposalRepositoryPort.java`, `infrastructure/adapter/out/persistence/ProposalMongoRepository.java` | The interface is in `application`, the implementation is in `infrastructure`. | [ ] |
| `controller/ProposalController.java` | `infrastructure/adapter/in/web/ProposalController.java` | Remains the entry point but now calls use case interfaces instead of a monolithic service. | [ ] |
| `scheduler/DataSyncScheduler.java` | `infrastructure/adapter/in/scheduler/ProposalSyncScheduler.java` | Remains the scheduler but now calls use case interfaces. | [ ] |
