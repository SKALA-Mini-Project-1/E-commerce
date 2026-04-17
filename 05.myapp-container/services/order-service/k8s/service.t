apiVersion: v1
kind: Service
metadata:
  name: ${USER_NAME}-${SERVICE_NAME}
  namespace: ${NAMESPACE}
  labels:
    app.kubernetes.io/name: ${SERVICE_NAME}
    app.kubernetes.io/instance: ${USER_NAME}-${SERVICE_NAME}
spec:
  selector:
    app.kubernetes.io/instance: ${USER_NAME}-${SERVICE_NAME}
  ports:
    - name: http
      protocol: TCP
      port: 8080
      targetPort: http
  type: ClusterIP
