#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

# Helm 저장소 추가
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

#TEST="--dry-run --debug"

helm upgrade --install ${USER_NAME}-mongodb bitnami/mongodb \
    --namespace ${NAMESPACE} \
    --version 16.5.11 \
    --set global.security.allowInsecureImages=true \
    -f custom-values.yaml
