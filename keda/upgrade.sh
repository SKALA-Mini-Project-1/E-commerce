#!/bin/bash

helm repo add kedacore https://kedacore.github.io/charts
helm repo update

helm upgrade --install keda kedacore/keda --version 2.16.1 -f upgrade-values.yaml -n keda
