apiVersion: v1
kind: ConfigMap
metadata:
  name: ${USER_NAME}-${SERVICE_NAME}-config
  namespace: ${NAMESPACE}
  labels:
    app.kubernetes.io/name: ${SERVICE_NAME}
    app.kubernetes.io/instance: ${USER_NAME}-${SERVICE_NAME}
data:
  SPRING_PROFILES_ACTIVE: "mariadb"
  JAVA_OPTS: "-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
  MARIADB_HOST: "${MARIADB_HOST}"
  MARIADB_PORT: "${MARIADB_PORT}"
  PAYMENT_SERVICE_DB: "payment_service_db"
  ORDER_SERVICE_BASE_URL: "http://${USER_NAME}-order-service:8080"
