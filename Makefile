# ============================================================================
# Backend Commands
# ============================================================================

.PHONY: api-dev
api-dev: ## Run API in development mode (requires MySQL running)
	cd apps/api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ============================================================================
# Database Commands
# ============================================================================

.PHONY: db-dump
db-dump: ## Create a SQL dump of the database (output: rooflet_dump.sql)
	docker exec rooflet-mysql mysqldump -u root rooflet_db > sql/rooflet_dump.sql
	@echo "Database dumped to rooflet_dump.sql"

.PHONY: db-reseed
db-reseed: ## Drop and recreate database, then restore from dump file (WARNING: destroys all data)
	docker exec rooflet-mysql mysql -u root -e "DROP DATABASE IF EXISTS rooflet_db; CREATE DATABASE rooflet_db;"
	@echo "Database dropped and recreated"
	docker exec -i rooflet-mysql mysql -u root rooflet_db < sql/rooflet_dump.sql
	@echo "Database reseeded successfully from rooflet_dump.sql"

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
