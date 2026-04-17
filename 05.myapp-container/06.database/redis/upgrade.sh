#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

helm upgrade ${USER_NAME}-redis bitnami/redis \
  --namespace ${NAMESPACE} \
  -f custom-values.yaml

