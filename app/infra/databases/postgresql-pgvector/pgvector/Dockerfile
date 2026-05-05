FROM pgvector/pgvector:pg17 AS builder

# bitnamilegacy/postgresql 17.5.0-debian-12-r3 (bitnami helm chart 16.7.4 기본 이미지)
FROM bitnamilegacy/postgresql:17.5.0-debian-12-r3

USER root
COPY --from=builder /usr/lib/postgresql/17/lib/vector.so /opt/bitnami/postgresql/lib/
COPY --from=builder /usr/share/postgresql/17/extension/vector.control /opt/bitnami/postgresql/share/extension/
COPY --from=builder /usr/share/postgresql/17/extension/vector--*.sql /opt/bitnami/postgresql/share/extension/
USER 1001

