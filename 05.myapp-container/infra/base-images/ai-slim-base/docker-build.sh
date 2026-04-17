#!/bin/bash
IMAGE_NAME="ai-common-base"
VERSION="1.0.0"

CPU_PLATFORM=amd64

# Docker 이미지 빌드
docker buildx build \
  --tag ${IMAGE_NAME}:${VERSION} \
  --file Dockerfile \
  --platform linux/${CPU_PLATFORM} \
  ${IS_CACHE} .
