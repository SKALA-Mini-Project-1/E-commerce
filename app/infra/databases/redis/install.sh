#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

# Helm 저장소 추가
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update


#TEST="--dry-run --debug"

helm upgrade --install ${USER_NAME}-redis bitnami/redis \
    --namespace ${NAMESPACE} \
    --version 21.1.6 \
    -f custom-values.yaml
