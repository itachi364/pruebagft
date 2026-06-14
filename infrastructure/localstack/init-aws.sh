#!/bin/sh
set -eu

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

BUCKET_NAME="${S3_BUCKET_NAME:-file-renaming-input-local}"
RULES_TABLE="${RULES_TABLE_NAME:-file-renaming-rules-local}"
BATCHES_TABLE="${BATCHES_TABLE_NAME:-file-renaming-batches-local}"
RESULTS_TABLE="${RESULTS_TABLE_NAME:-file-renaming-results-local}"

awslocal s3api create-bucket --bucket "$BUCKET_NAME" >/dev/null 2>&1 || true

awslocal dynamodb create-table \
  --table-name "$RULES_TABLE" \
  --attribute-definitions AttributeName=ruleId,AttributeType=S AttributeName=version,AttributeType=N \
  --key-schema AttributeName=ruleId,KeyType=HASH AttributeName=version,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST >/dev/null 2>&1 || true

awslocal dynamodb create-table \
  --table-name "$BATCHES_TABLE" \
  --attribute-definitions AttributeName=batchId,AttributeType=S \
  --key-schema AttributeName=batchId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST >/dev/null 2>&1 || true

awslocal dynamodb create-table \
  --table-name "$RESULTS_TABLE" \
  --attribute-definitions AttributeName=batchId,AttributeType=S AttributeName=resultId,AttributeType=S \
  --key-schema AttributeName=batchId,KeyType=HASH AttributeName=resultId,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST >/dev/null 2>&1 || true

for file_name in \
  PHO_CD_DES_20260430 \
  PHO_SV_20260430 \
  PHO_CK_20260430 \
  PHO_ML_UTIL_20260430.txt \
  cuotas_bdb_20260430.txt \
  garantias_solo_firma_20263004.txt \
  activos_vehiculo_20260430.txt \
  PrendasPajaro.txt
do
  printf "sample" > "/tmp/$file_name"
  awslocal s3api put-object --bucket "$BUCKET_NAME" --key "$file_name" --body "/tmp/$file_name" >/dev/null
done

put_rule() {
  awslocal dynamodb put-item --table-name "$RULES_TABLE" --item "$1" >/dev/null
}

put_rule '{"ruleId":{"S":"rule-pho-cd-des"},"version":{"N":"1"},"name":{"S":"CDT Desmaterializado"},"sourcePattern":{"S":"PHO_CD_DES_*"},"targetTemplate":{"S":"01_Estructura CDT Desmaterializado_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"AUTO"},"priority":{"N":"10"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-pho-sv"},"version":{"N":"1"},"name":{"S":"Cuenta Ahorros"},"sourcePattern":{"S":"PHO_SV_*"},"targetTemplate":{"S":"03_Estructura Cuenta Ahorros_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"AUTO"},"priority":{"N":"20"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-pho-ck"},"version":{"N":"1"},"name":{"S":"Cuenta Corriente"},"sourcePattern":{"S":"PHO_CK_*"},"targetTemplate":{"S":"04_Estructura Cuenta Corriente_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"AUTO"},"priority":{"N":"30"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-pho-ml-util"},"version":{"N":"1"},"name":{"S":"Creditos Utilizacion"},"sourcePattern":{"S":"PHO_ML_UTIL_*"},"targetTemplate":{"S":"13_CREDITOS UTILIZACION_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"AUTO"},"priority":{"N":"40"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-cuotas-bdb"},"version":{"N":"1"},"name":{"S":"Cuotas Activos"},"sourcePattern":{"S":"cuotas_bdb_*"},"targetTemplate":{"S":"13_CUOTAS Activos"},"requiresDate":{"BOOL":false},"dateStrategy":{"S":"NONE"},"priority":{"N":"50"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-garantias-solo-firma"},"version":{"N":"1"},"name":{"S":"Garantias Solo Firma"},"sourcePattern":{"S":"garantias_solo_firma_*"},"targetTemplate":{"S":"14_Solo Firma_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"YYYYDDMM"},"priority":{"N":"60"},"active":{"BOOL":true}}'
put_rule '{"ruleId":{"S":"rule-activos-vehiculo"},"version":{"N":"1"},"name":{"S":"Leasing Vehiculo"},"sourcePattern":{"S":"activos_vehiculo_*"},"targetTemplate":{"S":"37_Leasing_Vehiculo_{date}"},"requiresDate":{"BOOL":true},"dateStrategy":{"S":"AUTO"},"priority":{"N":"70"},"active":{"BOOL":true}}'

echo "LocalStack resources initialized."

