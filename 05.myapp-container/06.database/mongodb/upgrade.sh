#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

helm upgrade ${USER_NAME}-mongodb bitnami/mongodb \
  --namespace ${NAMESPACE} \
  -f custom-values.yaml

