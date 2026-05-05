#!/bin/bash

USER_NAME=sk199
NAMESPACE=skala-practice

helm upgrade ${USER_NAME}-postgres bitnami/postgresql \
  --namespace ${NAMESPACE} \
  -f custom-values.yaml

