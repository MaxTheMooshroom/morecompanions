#!/usr/bin/env bash

declare -g RED="\033[31m"
declare -g YELLOW="\033[33m"
declare -g GREEN="\033[32m"
declare -g RESET="\033[0m"

[[ ! -f .env ]] && echo -e "BIN_PATH=./build/libs\nJAR_NAME=\nINSTALL_PATH=\n" > .env

source .env

function err {
    [[ $# -lt 1 || -z $1 ]] && err "err is missing arg 1: err_msg"    251
    [[ $# -lt 2 || -z $2 ]] && err "err is missing arg 2: exit code"  252

    echo -e "${RED}[ERROR]${RESET}: ${YELLOW}$1${RESET}" >&2
    exit "$2"
}

function assert_exists {
  [[ $# -lt 1 || -z $1 ]] && err "assert_exists is missing arg 1: var_name"   241
  [[ $# -lt 2 || -z $2 ]] && err "assert_exists is missing arg 2: exit code"  242
  code=$2

  if ! declare -p "$1" >/dev/null 2>&1; then
    err "no ${1} var" "$2"
  elif [[ -z "${!1}" ]]; then
    err "empty var $1" "$(( code++ ))"
  fi
}

function main {
  assert_exists BIN_PATH 01
  [[ ! -d ${BIN_PATH} ]] && err "BIN_PATH ('${BIN_PATH}') does not exist" 02

  assert_exists JAR_NAME 11
  [[ ! -f ${BIN_PATH}/${JAR_NAME} ]] && err "qualified path ('${BIN_PATH}/${JAR_NAME}') does not exist" 12
  local qual_path="${BIN_PATH}/${JAR_NAME}"

  assert_exists INSTALL_PATH 21
  [[ ! -e ${INSTALL_PATH} ]]  && err "INSTALL_PATH ('${INSTALL_PATH}') does not exist"              22
  [[ ! -d ${INSTALL_PATH} ]]  && err "INSTALL_PATH ('${INSTALL_PATH}') is not a directory"          23

  echo -e "${YELLOW}\"${JAR_NAME}\"${RESET} -> ${GREEN}\"$(dirname "${INSTALL_PATH}")\"${RESET}"
  cp "${qual_path}" "${INSTALL_PATH}"
}

main
