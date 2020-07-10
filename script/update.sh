#!/bin/bash

set -e

workdir=$(cd $(dirname $0); pwd)
cd ${workdir}
echo "Current Dir: $workdir"

# 参数设定
SQLITE3_CMD="/usr/bin/sqlite3"
MYSQL_CMD=`cat db.conf`
DB_URL="https://r.llsif.win/db/"
mkdir -p bak
mkdir -p logs

# 记录日志
function wlog() {
    local wlog_date=`date +"%Y-%m-%d %H:%M:%S"`
    local wlog_suffix=`date +"%Y-%m-%d"`
    echo "$wlog_date $1"
    echo "$wlog_date $1" >> logs/update.log.$wlog_suffix
}

# 执行更新任务
function update() {
    local file=$1
    suffix=`date +"%Y%m%d%H%M%S"`
    mv ${file}.db_ bak/${file}.db_.$suffix
    mv ${file}.sql bak/${file}.sql.$suffix
    download ${file}
    if [ ! -f "${file}.db_" ]; then
        echo "Download ${file}.db_ failed, exit!"
        exit 1
    fi
    transform ${file}.db_ ${file}.sql
    execute ${file}.sql
}

# 下载db文件
function download() {
    local file=$1
    wlog "Downloding file from ${DB_URL}/${file}/${file}.db_ ..."
    wget "${DB_URL}/${file}/${file}.db_"
    wlog "Download finish!"
}

# 转换为MYSQL格式
function transform() {
    local dbfile=$1
    local file=$2
    wlog "Transforming file $dbfile to $file ..."
    $SQLITE3_CMD $dbfile .dump > $file
    # 删除首行
    sed -i '1d' $file
    # 替换BEGIN TRANSACTION
    sed -i '1c BEGIN;' $file
    # 替换CREATE TABLE
    sed -i 's/CREATE TABLE `\(.*\)`/DROP TABLE IF EXISTS `\1`; CREATE TABLE IF NOT EXISTS `\1`/g' $file
    # 替换双引号
    sed -i 's/INSERT INTO "/INSERT INTO /g' $file
    sed -i 's/" VALUES/ VALUES/g' $file
    # 替换TEXT
    sed -i 's/TEXT/VARCHAR(128)/g' $file
    # 自定义替换字段
    sed -i 's/`release_tag` VARCHAR(128)/`release_tag` VARCHAR(4096)/g' $file
    sed -i 's/`long_description` VARCHAR(128)/`long_description` VARCHAR(1024)/g' $file
    wlog "Transform finish!"
}

# 执行入库
function execute() {
    local file=$1
    wlog "Executing file: $file"
    $MYSQL_CMD < $file
    wlog "Execute finish!"
}

update live
update unit
wlog "Update finish!"
