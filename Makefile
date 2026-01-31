# ============================================================================
# Backend Commands
# ============================================================================

.PHONY: api-dev
api-dev: ## Run API in development mode (requires MySQL running)
	cd apps/api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ============================================================================
# Frontend Commands
# ============================================================================

.PHONY: web-dev
web-dev: ## Run web UI in development mode
	cd apps/web && bun run dev

.PHONY: web-install
web-install: ## Install web UI dependencies
	cd apps/web && bun install

.PHONY: web-build
web-build: ## Build web UI for production
	cd apps/web && bun run build

.PHONY: web-start
web-start: ## Start production web server
	cd apps/web && bun run start

.PHONY: web-test
web-test: ## Run web UI tests
	cd apps/web && bun run test
