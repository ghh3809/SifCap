#!/bin/bash

set -e

workdir=$(cd $(dirname $0); pwd)
cd ${workdir}
echo "Current Dir: $workdir"

# 参数设定
SQLITE3_CMD="/usr/bin/sqlite3"
MYSQL_CMD=`cat db.conf`
DB_URL_CN="https://card.niconi.co.ni/db/"
DB_URL_JP="https://r.llsif.win/db/"
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
    local lang=$1
    local file=$2
    local subfile=${3:-$file}
    suffix=`date +"%Y%m%d%H%M%S"`
    rm -f ${subfile}.db_
    rm -f ${subfile}.sql
    download ${lang} ${file} ${subfile}
    if [ ! -f "${subfile}.db_" ]; then
        echo "Download ${subfile}.db_ failed, exit!"
        exit 1
    fi
    transform ${lang} ${subfile}.db_ ${subfile}.sql
    execute ${subfile}.sql
    mv ${subfile}.db_ bak/${subfile}-${lang}.db_.$suffix
    mv ${subfile}.sql bak/${subfile}-${lang}.sql.$suffix
}

# 下载db文件
function download() {
    local lang=$1
    local file=$2
    local subfile=${3:-$file}
    if [ "$lang" == "cn" ]; then
        DB_URL=$DB_URL_CN
    else
        DB_URL=$DB_URL_JP
    fi
    wlog "Downloding file from ${DB_URL}/${file}/${subfile}.db_ ..."
    curl -b "dbLocalize=CN" "${DB_URL}/${file}/${subfile}.db_" > ${subfile}.db_
    wlog "Download finish!"
}

# 转换为MYSQL格式
function transform() {
    local lang=$1
    local dbfile=$2
    local file=$3
    wlog "Transforming file $dbfile to $file ..."
    $SQLITE3_CMD $dbfile .dump > $file
    # 删除首行
    sed -i '1d' $file
    # 替换BEGIN TRANSACTION
    sed -i '1c BEGIN;' $file
    # 替换CREATE TABLE
    sed -i 's/CREATE TABLE `\(.*\)`/DROP TABLE IF EXISTS `\1_'$lang'`; CREATE TABLE IF NOT EXISTS `\1_'$lang'`/g' $file
    # 替换双引号
    sed -i 's/INSERT INTO "\(.*\)" VALUES/INSERT INTO \1_'$lang' VALUES/g' $file
    # 替换index
    sed -i 's/CREATE INDEX `\(.*\)` ON `\(.*\)`(/CREATE INDEX `\1` ON `\2_'$lang'`(/g' $file
    # 替换TEXT
    sed -i 's/TEXT/VARCHAR(128)/g' $file
    # 自定义替换字段
    sed -i 's/`release_tag` VARCHAR(128)/`release_tag` VARCHAR(4096)/g' $file
    sed -i 's/`long_description` VARCHAR(128)/`long_description` VARCHAR(1024)/g' $file
    sed -i '/INSERT INTO strings_m_/d' $file
    wlog "Transform finish!"
}

# 执行入库
function execute() {
    local file=$1
    wlog "Executing file: $file"
    $MYSQL_CMD < $file
    wlog "Execute finish!"
}

for lang in 'cn' 'jp'; do
    wlog "Executing language for: $lang"
    update $lang live
    update $lang unit
    #update $lang common game_mater
    update $lang item
    #update $lang effort
done
wlog "Update finish!"
