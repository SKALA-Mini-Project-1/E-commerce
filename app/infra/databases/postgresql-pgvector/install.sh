#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

# Helm 저장소 추가
#helm repo add bitnami https://charts.bitnami.com/bitnami
#helm repo update


#TEST="--dry-run --debug"

helm upgrade --install ${USER_NAME}-postgres bitnami/postgresql \
  --namespace ${NAMESPACE} \
  --version 16.7.4 \
  --set global.security.allowInsecureImages=true \
  -f custom-values.yaml
