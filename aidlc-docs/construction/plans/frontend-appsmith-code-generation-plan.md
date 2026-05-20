# Code Generation Plan - Frontend Appsmith

## Unit Context
- **Unit**: Frontend Appsmith (CRM Lead Management POC)
- **Platform**: Appsmith Cloud (app.appsmith.com)
- **Connectivity**: ngrok tunnel → localhost:8090 (Domain Service)
- **Scope**: 4 pages + navigation + JSON export + guides

## Dependencies
- Domain Service (existing, running on localhost:8090)
- Camunda 8 (existing, running on Docker)
- ngrok (to be installed)
- Appsmith Cloud account (existing, unused)

## Output Location
- **Application artifacts**: `frontend-appsmith/` (workspace root)
- **Documentation**: `aidlc-docs/construction/frontend-appsmith/code/`

---

## Code Generation Steps

### Step 1: Ngrok Setup Guide
- [x] Create `frontend-appsmith/docs/ngrok-setup.md`
- [x] Include: installation instructions (macOS brew/download)
- [x] Include: command to start tunnel (`ngrok http 8090`)
- [x] Include: how to get public URL
- [x] Include: notes about free tier limitations (2h session, URL changes)
- [x] Include: tips for stable development (ngrok config file)

### Step 2: Appsmith Cloud Datasource Configuration Guide
- [x] Create `frontend-appsmith/docs/datasource-setup.md`
- [x] Include: login to app.appsmith.com
- [x] Include: create new application
- [x] Include: create REST API datasource with ngrok URL
- [x] Include: test connection
- [x] Include: CORS verification notes

### Step 3: Page 1 - Lead List Implementation
- [x] Document page layout and widgets in guide
- [x] Create API query: GET {{datasource}}/api/leads
- [x] Table widget configuration (columns, sorting, formatting)
- [x] Status badge color mapping (JS logic)
- [x] Empty state handling
- [x] Row click → navigate to Lead Detail with leadId
- [x] Include in JSON export

### Step 4: Page 2 - Lead Detail Implementation
- [x] Document page layout and widgets in guide
- [x] Create API query: GET {{datasource}}/api/leads/{{leadId}}
- [x] URL parameter handling (leadId from navigation)
- [x] Customer info display (Text widgets)
- [x] Progress bar widget for status visualization
- [x] History table widget
- [x] Status update dropdown (filtered by state machine)
- [x] Update button → PUT /api/leads/{id}/status
- [x] Success/error handling + data refresh
- [x] Include in JSON export

### Step 5: Page 3 - Lead Allocation Implementation
- [x] Document page layout and widgets in guide
- [x] Create API query: GET {{datasource}}/api/leads/allocatable?ownerId=USR-MGR-01
- [x] Table with row selection (checkbox)
- [x] Select All / Deselect All buttons
- [x] Allocate button → open Modal
- [x] Modal: API GET /api/users/subordinates
- [x] Modal: User checkbox list
- [x] Warning message when >= 2 users selected
- [x] Allocate button → POST /api/leads/allocate
- [x] Result display + list refresh
- [x] Include in JSON export

### Step 6: Page 4 - Dynamic Form (Workflow Tasks) Implementation
- [x] Document page layout and widgets in guide
- [x] Create API query: GET {{datasource}}/api/workflow/tasks
- [x] Task list display (Table or List widget)
- [x] Task selection → GET /api/forms/{taskType}
- [x] Dynamic form rendering strategy (JSON Form widget or custom JS)
- [x] Field type mapping: TEXT→Input, TEXTAREA→RichText, DROPDOWN→Select, etc.
- [x] Visibility condition logic (JS in widget visibility property)
- [x] Validation rules implementation
- [x] Submit button → POST /api/workflow/tasks/{jobKey}/complete
- [x] Success handling + task list refresh
- [x] Include in JSON export

### Step 7: Navigation & Layout Configuration
- [x] Sidebar menu configuration (4 pages)
- [x] Application name: "CRM Lead Management"
- [x] Page naming and ordering
- [x] Consistent theme/styling
- [x] Include in JSON export

### Step 8: Appsmith JSON Export File
- [x] Create `frontend-appsmith/appsmith-export.json`
- [x] Include all 4 pages with queries, widgets, JS objects
- [x] Include datasource configuration (placeholder URL)
- [x] Validate JSON structure matches Appsmith import format
- [x] Add README note about replacing datasource URL after import

### Step 9: Comprehensive Build Guide
- [x] Create `frontend-appsmith/docs/appsmith-build-guide.md`
- [x] Consolidate all steps into single comprehensive guide
- [x] Include screenshots descriptions (widget placement, property settings)
- [x] Include JS code snippets for dynamic logic
- [x] Include troubleshooting section

### Step 10: Platform Evaluation Template
- [x] Create `frontend-appsmith/docs/evaluation-notes.md`
- [x] Template for recording: setup time, dynamic form capability, conditional fields, performance, export/import
- [x] Comparison criteria aligned with FR-07

### Step 11: Update README
- [x] Update `frontend-appsmith/README.md` with Appsmith Cloud approach
- [x] Remove Docker references
- [x] Add ngrok tunnel architecture
- [x] Add quick start instructions

### Step 12: Code Summary Documentation
- [x] Create `aidlc-docs/construction/frontend-appsmith/code/code-generation-summary.md`
- [x] List all generated files
- [x] Document key decisions made during generation
- [x] Note any limitations or workarounds

---

## Story Traceability

| Step | Requirements Covered |
|---|---|
| Step 1 | FA-01.1, NFA-01.2, NFA-01.3 |
| Step 2 | FA-01.2, FA-01.3, FA-01.4 |
| Step 3 | FA-02.1 - FA-02.7 |
| Step 4 | FA-03.1 - FA-03.9 |
| Step 5 | FA-04.1 - FA-04.9 |
| Step 6 | FA-05.1 - FA-05.9 |
| Step 7 | FA-06.1 - FA-06.4 |
| Step 8 | NFA-02.1 |
| Step 9 | NFA-02.2 |
| Step 10 | NFA-03.1 - NFA-03.5 |
| Step 11 | General documentation |
| Step 12 | Internal tracking |

---

## Total: 12 Steps
## Estimated Scope: Medium (lowcode platform, mostly configuration + JS snippets)
