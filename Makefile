# Tika Service Makefile
# Maintainer: h00dieB0y
# Date:       2025-06-15

#─── Configurable Variables ─────────────────────────────────────────────────────
MAVEN        ?= ./mvnw
DC           ?= docker compose
ENV_FILE     ?= .env
ENV_TEMPLATE ?= .env.template
SKIP_TESTS   ?= true

#─── Derived Flags ──────────────────────────────────────────────────────────────
ifeq ($(SKIP_TESTS),true)
	MVN_TEST_FLAG = -DskipTests
endif

#─── Defaults & Shell Setup ────────────────────────────────────────────────────
.DEFAULT_GOAL := help
.SHELLFLAGS   := -eu -o pipefail -c

#─── Color Codes (optional) ─────────────────────────────────────────────────────
GREEN  := $(shell tput setaf 2 2>/dev/null || echo "")
YELLOW := $(shell tput setaf 3 2>/dev/null || echo "")
BLUE   := $(shell tput setaf 4 2>/dev/null || echo "")
RESET  := $(shell tput sgr0    2>/dev/null || echo "")

#─── Phony Targets ──────────────────────────────────────────────────────────────
.PHONY: help clean build test static-checks fmt lint-docker \
				docker-build docker-up docker-down docker-logs docker-ps \
				db-up redis-up kafka-up dev run install dependencies \
				update-dependencies generate-api-docs sonar \
				check-requirements ensure-env status prune

#─── Help ──────────────────────────────────────────────────────────────────────
help: ## Show this help message
	@echo ""
	@echo "Usage: ${GREEN}make <target>${RESET}"
	@echo ""
	@echo "${BLUE}Main Targets:${RESET}"
	@echo "  ${YELLOW}setup${RESET}            Initialize env, infra & build"
	@echo "  ${YELLOW}dev${RESET}              Start only infra (db/redis/kafka)"
	@echo "  ${YELLOW}run${RESET}              Launch Spring Boot API"
	@echo "  ${YELLOW}test${RESET}             Run Maven tests"
	@echo ""
	@echo "${BLUE}Other Targets:${RESET}"
	@awk 'BEGIN {FS = ":.*?## "} \
			 /^[a-zA-Z0-9_-]+:.*?## / { \
				 printf "  ${YELLOW}%-18s${RESET} %s\n", $$1, $$2 \
			 }' $(MAKEFILE_LIST)

#─── Build & Clean ─────────────────────────────────────────────────────────────
clean: ## Clean the Maven project
	@echo "${BLUE}Cleaning…${RESET}"
	@$(MAVEN) clean

build: ensure-env ## Compile & package (skip tests by default)
	@echo "${BLUE}Building…${RESET}"
	@$(MAVEN) clean package $(MVN_TEST_FLAG)

#─── Testing & Analysis ────────────────────────────────────────────────────────
test: ## Run all tests
	@echo "${BLUE}Testing…${RESET}"
	@$(MAVEN) test

static-checks: checkstyle pmd spotbugs ## Run all static code checks

checkstyle: ## Run Checkstyle
	@echo "${BLUE}Checkstyle…${RESET}"
	@$(MAVEN) checkstyle:check

pmd: ## Run PMD
	@echo "${BLUE}PMD…${RESET}"
	@$(MAVEN) pmd:check

spotbugs: ## Run SpotBugs
	@echo "${BLUE}SpotBugs…${RESET}"
	@$(MAVEN) spotbugs:check

fmt: ## Apply code formatting (Spotless)
	@echo "${BLUE}Formatting…${RESET}"
	@$(MAVEN) spotless:apply

lint-docker: ## Lint Dockerfile (requires hadolint)
	@echo "${BLUE}Linting Dockerfile…${RESET}"
	@hadolint Dockerfile

#─── Docker Infra ──────────────────────────────────────────────────────────────
docker-build: build ## Build all Docker images
	@echo "${BLUE}Docker building…${RESET}"
	@$(DC) build

docker-up: ensure-env check-requirements ## Start all containers
	@echo "${BLUE}Docker up…${RESET}"
	@$(DC) up -d

docker-down: ## Stop & remove containers
	@echo "${BLUE}Docker down…${RESET}"
	@$(DC) down

docker-logs: ## Follow container logs
	@$(DC) logs -f

docker-ps: ## List running containers
	@$(DC) ps

db-up: ## Start only Postgres
	@$(DC) up -d postgres

redis-up: ## Start only Redis
	@$(DC) up -d redis

kafka-up: ## Start only Kafka (KRaft)
	@$(DC) up -d kafka

#─── Development & Run ─────────────────────────────────────────────────────────
dev: db-up redis-up kafka-up ## Spin up infra for local dev
	@echo "${GREEN}Dev infra is ready!${RESET}"

run: ## Run the Spring Boot API
	@echo "${BLUE}Starting API…${RESET}"
	@$(MAVEN) spring-boot:run -pl tika-api

#─── Maven Utility ─────────────────────────────────────────────────────────────
install: ## Install to local Maven repo
	@$(MAVEN) clean install $(MVN_TEST_FLAG)

dependencies: ## Show dependency tree
	@$(MAVEN) dependency:tree

update-dependencies: ## Check for newer dependencies
	@$(MAVEN) versions:display-dependency-updates

generate-api-docs: ## Launch API docs profile
	@$(MAVEN) spring-boot:run -pl tika-api -Dspring-boot.run.profiles=docs

sonar: ## Run SonarCloud analysis
	@$(MAVEN) verify sonar:sonar \
		-Dsonar.projectKey=jarhead-killgrave_tika-service

#─── Utilities ────────────────────────────────────────────────────────────────
check-requirements: ## Verify Docker, Docker Compose & Java are installed
	@command -v docker >/dev/null 2>&1 || { \
		echo "${YELLOW}Docker not found.${RESET}"; exit 1; }
	@command -v $(DC) >/dev/null 2>&1 || { \
		echo "${YELLOW}‘$(DC)’ not found.${RESET}"; exit 1; }
	@command -v java >/dev/null 2>&1 || { \
		echo "${YELLOW}Java not found.${RESET}"; exit 1; }
	@echo "${GREEN}All tools are present.${RESET}"

#─── Environment Checks ───────────────────────────────────────────────────────
ensure-env: env-check ## Ensure .env file exists
	@if [ ! -f $(ENV_FILE) ]; then \
		echo "${YELLOW}Creating $(ENV_FILE) from template…${RESET}"; \
		cp $(ENV_TEMPLATE) $(ENV_FILE); \
	fi
	@echo "${BLUE}Using environment file: $(ENV_FILE)${RESET}"

status: docker-ps env-check ## Quick health-check
	@echo ""
	@echo "${GREEN}✓ Services are up${RESET}"

env-check: ## Warn if .env is missing
	@if [ ! -f $(ENV_FILE) ]; then \
		echo "${YELLOW}Warning: $(ENV_FILE) not found!${RESET}"; \
	else \
		echo "${GREEN}Environment file exists.${RESET}"; \
	fi

prune: ## Prune unused Docker objects
	@echo "${BLUE}Pruning Docker…${RESET}"
	@docker system prune --volumes -f

#─── All-in-One Setup ─────────────────────────────────────────────────────────
setup: ensure-env install docker-down docker-up build ## Bootstrap dev
	@echo "${GREEN}🚀 Setup complete!${RESET}"
