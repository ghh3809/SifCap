#!/bin/bash

# shellcheck disable=SC2046
# shellcheck disable=SC2164
WORK_DIR=$(cd $(dirname "$0"); pwd)
cd "${WORK_DIR}" || exit
echo "Current Dir: $WORK_DIR"

###########
#  设置项  #
###########
JAVA_CMD="java"
APP_NAME="SifCap"

# 初始化
APP_FILE="${APP_NAME}.jar"
APP_PATH="${WORK_DIR}/${APP_FILE}"
echo "$APP_PATH"

# kill已存在进程
pid=$(ps -ef | grep -v grep | grep $APP_FILE | awk '{print $2}')
if [ -n "$pid" ]; then
    echo "$pid"
    kill -9 "$pid"
fi

echo "starting app....."
nohup ${JAVA_CMD} -jar "${APP_PATH}" >/dev/null 2>&1 &
echo "app started ok !"

