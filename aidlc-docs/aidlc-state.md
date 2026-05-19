# AI-DLC State Tracking

## Project Information
- **Project Type**: Greenfield
- **Start Date**: 2026-05-19T00:00:00Z
- **Current Stage**: CONSTRUCTION - Build and Test (Pending Docker)

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
- [ ] Build and Test (IN PROGRESS - Pending Docker restart)

### 🟡 OPERATIONS PHASE
- [ ] Operations - PLACEHOLDER

## Current Status
- **Build**: ✅ Maven compile SUCCESS (29 files)
- **Tests**: ✅ 10/10 PBT tests PASS (jqwik)
- **Docker**: ❌ Docker Desktop needs restart → user restarting machine
- **Next Step**: After restart → `docker-compose up -d` → deploy BPMN → verify APIs

## Resume Instructions
When user returns after restart:
1. Verify Docker Desktop is running: `docker version`
2. Build JAR: `mvn package -DskipTests` (in domain-service/)
3. Start stack: `docker-compose up -d` (in workspace root)
4. Wait for health checks (~60-90s)
5. Deploy BPMN: `./deploy.sh localhost:26500` (in bpmn-processes/scripts/)
6. Verify: `curl http://localhost:8090/api/leads`

## Key Paths
- **Maven**: `C:\Program Files\NetBeans-23\netbeans\java\maven\bin\mvn.cmd`
- **Docker**: `C:\Program Files\Docker\Docker\resources\bin\docker.exe`
- **Java**: Java 19 (compatible with Java 17 target)

## Architecture Summary
- **Domain Service**: Spring Boot 3.2.x, Java 17, Maven, port 8090
- **Camunda Zeebe**: port 26500 (gRPC), 8088 (REST)
- **Camunda Operate**: port 8083
- **Camunda Tasklist**: port 8084
- **Elasticsearch**: port 9200
- **Appsmith**: port 8080 (to be configured manually)
- **Budibase**: port 8081 (to be configured manually)
