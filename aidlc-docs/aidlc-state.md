# AI-DLC State Tracking

## Project Information
- **Project Type**: Greenfield
- **Start Date**: 2026-05-19T00:00:00Z
- **Current Stage**: CONSTRUCTION - Frontend Budibase Build and Test (COMPLETED)

## Workspace State
- **Existing Code**: Yes (generated)
- **Reverse Engineering Needed**: No
- **Workspace Root**: d:\PROJECT\POC\workspace\CRM

## Code Location Rules
- **Application Code**: Workspace root (NEVER in aidlc-docs/)
- **Documentation**: aidlc-docs/ only
- **Structure patterns**: See code-generation.md Critical Rules

## Extension Configuration
| Extension | Enabled | Decided At |
|---|---|---|
| Property-Based Testing | Yes (Full) | Requirements Analysis |
| Security Baseline | No | Requirements Analysis |

## Stage Progress
### 🔵 INCEPTION PHASE
- [x] Workspace Detection (COMPLETED)
- [x] Requirements Analysis (COMPLETED - Approved)
- [x] Workflow Planning (COMPLETED)
- [x] Application Design (COMPLETED)
- [x] Units Generation (COMPLETED)

### 🟢 CONSTRUCTION PHASE
- [x] Functional Design (COMPLETED - Domain Service)
- [x] NFR Requirements (COMPLETED - Domain Service)
- [x] Code Generation (COMPLETED - All units)
- [x] Build and Test (COMPLETED - All services running on macOS)
- [x] Frontend Appsmith - Code Generation (COMPLETED)
- [x] Frontend Appsmith - Build and Test (COMPLETED)

- [x] Frontend Budibase - Code Generation (COMPLETED)
- [x] Frontend Budibase - Build and Test (COMPLETED)

### 🟡 OPERATIONS PHASE
- [ ] Operations - PLACEHOLDER

## Current Status
- **Build**: ✅ Maven compile + package SUCCESS (29 files)
- **Tests**: ✅ 10/10 PBT tests PASS (jqwik)
- **Docker**: ✅ All 5 containers running (macOS)
- **BPMN**: ✅ lead-lifecycle v2 deployed
- **Process Instance**: ✅ 1 instance running (waiting at "Liên hệ khách hàng")
- **Environment**: macOS (switched from Windows)
- **Frontend Appsmith**: ✅ Code Generation + Build & Test COMPLETED
- **Frontend Budibase**: ✅ Code Generation + Build & Test COMPLETED

## Resume Instructions
Stack is running on macOS. If containers stopped:
1. `docker compose up -d` (in workspace root)
2. Wait ~60s for Elasticsearch healthy
3. `docker start crm-operate crm-tasklist crm-domain-service` (if not auto-started due to Zeebe health check)
4. Verify: `curl http://localhost:8090/api/leads`

## Key Paths
- **Maven**: `/Users/working/Setup/apache-maven-3.9.6/bin/mvn`
- **Docker**: docker (native macOS)
- **Java**: Java 23.0.1 (compatible with Java 17 target)
- **zbctl**: installed via npm (global)

## Architecture Summary
- **Domain Service**: Spring Boot 3.2.x, Java 17, Maven, port 8090
- **Camunda Zeebe**: port 26500 (gRPC), 8088 (REST)
- **Camunda Operate**: port 8083
- **Camunda Tasklist**: port 8084
- **Elasticsearch**: port 9200
- **Appsmith**: port 8080 (to be configured manually)
- **Budibase**: port 8081 (to be configured manually)
