apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${USER_NAME}-${SERVICE_NAME}
  namespace: ${NAMESPACE}
  annotations:
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  ingressClassName: ${INGRESS_CLASS_NAME}
  rules:
    - host: ${INGRESS_HOST}
      http:
        paths:
          - path: /${SERVICE_NAME}(/|$)(.*)
            pathType: ImplementationSpecific
            backend:
              service:
                name: ${USER_NAME}-${SERVICE_NAME}
                port:
                  number: 8080
