#!/bin/bash
# PostgreSQL 외부 접속 진단 스크립트
#
USER_NAME=sk199
NAMESPACE=skala-practice

echo "=== PostgreSQL 외부 접속 진단 시작 ==="
echo "Timestamp: $(date)"
echo

# 1. Service 정보 확인
echo "1. Service 정보 확인"
echo "===================="
kubectl get svc -l app.kubernetes.io/name=postgresql -o wide
echo

# 3. Pod 상태 확인
echo "3. Pod 상태 확인"
echo "==============="
kubectl get pods -l app.kubernetes.io/name=postgresql -o wide
echo

# 4. Endpoints 확인
echo "4. Endpoints 확인"
echo "================"
kubectl get endpoints $SERVICE_NAME
echo

# 5. PostgreSQL 설정 확인
echo "5. PostgreSQL 설정 확인"
echo "======================"
POD_NAME=$(kubectl get pods -l app.kubernetes.io/name=postgresql -o jsonpath='{.items[0].metadata.name}')
echo "Pod Name: $POD_NAME"

echo "--- listen_addresses 확인 ---"
kubectl exec $POD_NAME -- psql -U postgres -c "SHOW listen_addresses;"

echo "--- port 확인 ---"
kubectl exec $POD_NAME -- psql -U postgres -c "SHOW port;"

echo "--- max_connections 확인 ---"
kubectl exec $POD_NAME -- psql -U postgres -c "SHOW max_connections;"

echo

# 6. pg_hba.conf 확인
echo "6. pg_hba.conf 확인"
echo "=================="
kubectl exec $POD_NAME -- cat /opt/bitnami/postgresql/conf/pg_hba.conf
echo

# 9. PostgreSQL 로그 확인
echo "9. PostgreSQL 로그 (최근 50줄)"
echo "============================="
kubectl logs $POD_NAME --tail=50
echo

echo "=== 진단 완료 ==="
