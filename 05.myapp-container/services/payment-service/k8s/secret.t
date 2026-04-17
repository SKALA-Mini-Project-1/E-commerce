apiVersion: v1
kind: Secret
metadata:
  name: ${USER_NAME}-${SERVICE_NAME}-secret
  namespace: ${NAMESPACE}
  labels:
    app.kubernetes.io/name: ${SERVICE_NAME}
    app.kubernetes.io/instance: ${USER_NAME}-${SERVICE_NAME}
type: Opaque
stringData:
  MARIADB_USERNAME: "${MARIADB_USERNAME}"
  MARIADB_PASSWORD: "${MARIADB_PASSWORD}"
