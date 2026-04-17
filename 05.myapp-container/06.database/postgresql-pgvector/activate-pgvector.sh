#!/bin/bash
# pgvector extension 활성화 및 동작 검증 스크립트
# 사용 시점:
#   - install.sh 이후 PVC가 이미 존재해서 initdb가 실행되지 않은 경우
#   - pgvector extension이 활성화되어 있는지 확인/보장하고 싶을 때

USER_NAME=sk199
NAMESPACE=skala-practice
POD="${USER_NAME}-postgres-postgresql-0"
TARGET_DB="cloud"

echo "=== pgvector Extension 활성화 및 검증 ==="
echo "Namespace : ${NAMESPACE}"
echo "Pod       : ${POD}"
echo "Database  : ${TARGET_DB}"
echo ""

# 1. Pod 상태 확인
echo "[1/4] Pod 상태 확인..."
kubectl get pod "${POD}" -n "${NAMESPACE}" --no-headers 2>/dev/null | grep -q "Running" || {
  echo "❌ Pod '${POD}'가 Running 상태가 아닙니다."
  kubectl get pod "${POD}" -n "${NAMESPACE}" 2>/dev/null
  exit 1
}
echo "✅ Pod Running 확인"
echo ""

# 2. extension 현재 상태 확인
echo "[2/4] pgvector extension 현재 상태 확인..."
CURRENT=$(kubectl exec -n "${NAMESPACE}" "${POD}" -- \
  psql -U postgres -d "${TARGET_DB}" -tAc \
  "SELECT extversion FROM pg_extension WHERE extname='vector';" 2>/dev/null)

if [ -n "${CURRENT}" ]; then
  echo "✅ 이미 활성화됨: vector ${CURRENT}"
else
  echo "⚠️  미활성화 상태. 활성화를 진행합니다..."
  echo ""

  # 3. extension 활성화
  echo "[3/4] CREATE EXTENSION vector 실행..."
  kubectl exec -n "${NAMESPACE}" "${POD}" -- \
    psql -U postgres -d "${TARGET_DB}" -c "CREATE EXTENSION IF NOT EXISTS vector;" 2>&1

  if [ $? -ne 0 ]; then
    echo "❌ extension 활성화 실패"
    exit 1
  fi
  echo "✅ extension 활성화 완료"
fi
echo ""

# 4. 동작 검증 (벡터 CRUD + 거리 계산)
echo "[4/4] pgvector 동작 검증..."
kubectl exec -n "${NAMESPACE}" "${POD}" -- psql -U postgres -d "${TARGET_DB}" -c "
-- 설치된 버전 확인
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';

-- 벡터 테이블 생성 / 데이터 삽입 / 거리 계산 / 정리
CREATE TEMP TABLE _pgvector_test (id serial, v vector(3));
INSERT INTO _pgvector_test (v) VALUES ('[1,2,3]'), ('[4,5,6]'), ('[7,8,9]');
SELECT id, v, v <-> '[1,2,3]' AS l2_distance FROM _pgvector_test ORDER BY l2_distance;
" 2>&1

if [ $? -eq 0 ]; then
  echo ""
  echo "✅ pgvector 동작 검증 완료"
else
  echo ""
  echo "❌ 검증 실패"
  exit 1
fi

echo ""
echo "=== 완료 ==="
echo "접속 정보:"
echo "  클러스터 내부: ${USER_NAME}-postgres-postgresql.${NAMESPACE}.svc.cluster.local:5432"
echo "  DB: ${TARGET_DB} / User: skala"
