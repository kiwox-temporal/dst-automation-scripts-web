#!/bin/bash

APP_NAME="dst_test"
APP_VERSION="0.0.15"

USER_TIMEZONE="America/Santiago"

ROOT_PATH=`dirname "$0"`

JAR_FILE=${ROOT_PATH}/target/${APP_NAME}-${APP_VERSION}.jar

JAVA_BIN=java

export CONF_DIR=${ROOT_PATH}/conf

LOGGING="-Dlogback.configurationFile=${CONF_DIR}/logback.xml"

$JAVA_BIN $LOGGING -Duser.timezone=$USER_TIMEZONE -jar $JAR_FILE app-entel-peru-verificar-saldo-post-pago "$@" -android "/Users/msaavedra/Library/Android/sdk" -appium "/usr/local/bin/appium" -node "/Users/msaavedra/.nvm/versions/node/v18.6.0/bin/node" -screenshot "/Users/msaavedra/Documents/personal" "$@"
