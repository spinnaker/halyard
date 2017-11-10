#!/usr/bin/env bash

GIT_ROOT={%git-root%}
ARTIFACT={%artifact%}
SCRIPTS_DIR={%scripts-dir%}

LOG_DIR=${GIT_ROOT}/logs
PID_FILE=${GIT_ROOT}/${ARTIFACT}.pid
ARTIFACT_DIR=${GIT_ROOT}/${ARTIFACT}
STOP_SCRIPT=${SCRIPTS_DIR}/${ARTIFACT}-stop.sh

START_COMMAND="{%start-command%}"

function start() {
  if [ ! -d $LOG_DIR ]; then
    mkdir -p $LOG_DIR
  fi

 ${STOP_SCRIPT}

  pushd $ARTIFACT_DIR
  $START_COMMAND \
    2> ${LOG_DIR}/${ARTIFACT}.err \
    > ${LOG_DIR}/${ARTIFACT}.log &

  echo $! > $PID_FILE
  popd
}

start
