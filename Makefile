# Source and target files/directories
PROJECT = $(shell grep projectName build.sc | cut -d= -f2|tr -d "\"" |xargs)
SCALA_FILES = $(wildcard $(PROJECT)/src/*.scala) $(wildcard $(PROJECT)/resources/*.scala) $(wildcard $(PROJECT)/test/src/*.scala)
GENERATED_FILES = generated
FIRTOOL_VERSION=1.32.0
MILL = ./mill
CHISELPARAMS = --split-verilog

FIRTOOL_URL :=
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	FIRTOOL_URL =https://github.com/llvm/circt/releases/download/firtool-$(FIRTOOL_VERSION)/circt-bin-ubuntu-20.04.tar.gz
endif
ifeq ($(UNAME_S),Darwin)
	FIRTOOL_URL = https://github.com/llvm/circt/releases/download/firtool-$(FIRTOOL_VERSION)/circt-bin-macos-11.tar.gz
endif
export PATH := ./:$(PATH) # so that we can use firtool

# Targets
all: chisel

chisel: $(GENERATED_FILES) ## Generates Verilog code from Chisel sources (output to ./generated)
$(GENERATED_FILES): $(SCALA_FILES) build.sc Makefile firtool
	@rm -rf $@
	$(MILL) $(PROJECT).run $(CHISELPARAMS) --target-dir $@

firtool:
	@curl -sL $(FIRTOOL_URL) |tar -xz
	@cp firtool-$(FIRTOOL_VERSION)/bin/firtool .

check: test
.PHONY: test
test: ## Run Chisel tests
	$(MILL) $(PROJECT).test

.PHONY: lint
lint: ## Formats code using scalafmt and scalafix
	$(MILL) lint

.PHONY: clean
clean: ## Clean all generated files
	$(MILL) clean
	@rm -rf obj_dir test_run_dir target
	@rm -rf $(GENERATED_FILES)
	@rm -rf tmphex
	@rm -rf out
	@rm -f *.mem

.PHONY: cleanall
cleanall: clean  ## Clean all downloaded dependencies and cache
	@rm -rf PROJECT/.bloop
	@rm -rf PROJECT/PROJECT
	@rm -rf PROJECT/target
	@rm -rf .bloop .bsp .metals .vscode

.PHONY: help
help:
	@echo "Makefile targets:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = "[:##]"}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$4}'
	@echo ""

.DEFAULT_GOAL := help