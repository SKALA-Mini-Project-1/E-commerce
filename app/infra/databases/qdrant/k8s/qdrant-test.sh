#!/bin/bash
# Qdrant Vector Database 기능 테스트 스크립트 (Kubernetes용)

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 사용법 출력
usage() {
    echo "사용법: $0 [OPTIONS]"
    echo ""
    echo "OPTIONS:"
    echo "  -n, --namespace NAMESPACE    Kubernetes namespace (기본값: qdrant)"
    echo "  -s, --service SERVICE        Service 이름 (기본값: qdrant)"
    echo "  -k, --api-key API_KEY        API Key (설정된 경우)"
    echo "  -p, --port-forward           Port-forward 사용 (기본값: Ingress)"
    echo "  -d, --delete                 테스트 후 컬렉션 삭제"
    echo "  -h, --help                   도움말 표시"
    echo ""
    echo "예시:"
    echo "  $0                                    # Ingress URL로 테스트 (기본)"
    echo "  $0 -p                                 # Port-forward로 테스트"
    echo "  $0 -k 'your-api-key'                  # Ingress + API Key 사용"
    echo "  $0 -k 'your-api-key' -d               # 테스트 후 컬렉션 삭제"
    exit 1
}

# 기본 설정
NAMESPACE="qdrant"
SERVICE_NAME="qdrant"
API_KEY=""
USE_PORT_FORWARD=false
DELETE_COLLECTION=false
INGRESS_HOST="qdrant.skala25a.project.skala-ai.com"

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE_NAME="$2"
            shift 2
            ;;
        -k|--api-key)
            API_KEY="$2"
            shift 2
            ;;
        -p|--port-forward)
            USE_PORT_FORWARD=true
            shift
            ;;
        -d|--delete)
            DELETE_COLLECTION=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "알 수 없는 옵션: $1"
            usage
            ;;
    esac
done

# Port forward 프로세스 ID 저장 변수
PORT_FORWARD_PID=""

# cleanup 함수 (종료 시 port-forward 정리)
cleanup() {
    if [ -n "$PORT_FORWARD_PID" ]; then
        echo ""
        echo -e "${YELLOW}Port forward 종료 중...${NC}"
        kill $PORT_FORWARD_PID 2>/dev/null
        wait $PORT_FORWARD_PID 2>/dev/null
    fi
}

# 스크립트 종료 시 cleanup 실행
trap cleanup EXIT INT TERM

# Kubernetes 리소스 확인
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Kubernetes 환경 확인${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 접속 방법에 따른 설정
if [ "$USE_PORT_FORWARD" = true ]; then
    # Port forward 사용
    echo ""
    echo -e "${YELLOW}Port forward 시작 (localhost:6333 -> ${SERVICE_NAME}:6333)...${NC}"
    kubectl port-forward -n ${NAMESPACE} svc/${SERVICE_NAME} 6333:6333 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!

    # Port forward 준비 대기
    sleep 3

    # 연결 확인
    if ! curl -s http://localhost:6333/ > /dev/null 2>&1; then
        echo -e "${RED}✗ Port forward 연결 실패${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Port forward 연결 성공"
    echo ""
    
    QDRANT_HOST="localhost"
    QDRANT_PORT="6333"
    BASE_URL="http://${QDRANT_HOST}:${QDRANT_PORT}"
else
    # Ingress 사용 (기본)
    echo ""
    echo -e "${YELLOW}Ingress를 통한 접속: https://${INGRESS_HOST}${NC}"
    
    # Ingress 확인
    if ! kubectl get ingress -n ${NAMESPACE} | grep -q qdrant; then
        echo -e "${RED}✗ Ingress가 존재하지 않습니다.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Ingress 확인 완료"
    
    # DNS 확인
    if ! nslookup ${INGRESS_HOST} > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠ DNS 레코드를 확인할 수 없습니다. 계속 진행합니다...${NC}"
    fi
    
    QDRANT_HOST="${INGRESS_HOST}"
    QDRANT_PORT="443"
    BASE_URL="https://${QDRANT_HOST}"
    
    # 연결 확인
    if ! curl -k -s ${BASE_URL}/ > /dev/null 2>&1; then
        echo -e "${RED}✗ Ingress 연결 실패${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Ingress 연결 성공"
    echo ""
fi

# 테스트 컬렉션 이름
TEST_COLLECTION="test_collection_$(date +%s)"

# 테스트 결과 카운터
PASSED=0
FAILED=0

# 함수: 테스트 결과 출력
print_test_result() {
    local test_name=$1
    local result=$2
    
    if [ "$result" -eq 0 ]; then
        echo -e "${GREEN}✓${NC} ${test_name}"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} ${test_name}"
        ((FAILED++))
    fi
}

# 함수: API 호출 (API Key 지원)
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    local headers=(-H 'Content-Type: application/json')
    local curl_opts=(-s)
    
    # HTTPS인 경우 인증서 검증 비활성화 (자체 서명 인증서 대응)
    if [[ "$BASE_URL" == https://* ]]; then
        curl_opts+=(-k)
    fi
    
    # API Key가 설정된 경우 헤더에 추가
    if [ -n "$API_KEY" ]; then
        headers+=(-H "api-key: ${API_KEY}")
    fi
    
    if [ -z "$data" ]; then
        curl "${curl_opts[@]}" -X ${method} "${BASE_URL}${endpoint}" "${headers[@]}"
    else
        curl "${curl_opts[@]}" -X ${method} "${BASE_URL}${endpoint}" "${headers[@]}" -d "${data}"
    fi
}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Qdrant Vector Database 테스트 시작${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 테스트 1: 서버 연결 확인
echo -e "${YELLOW}[테스트 1] 서버 연결 확인${NC}"
response=$(call_api GET "/")
if echo "$response" | grep -q "qdrant"; then
    print_test_result "서버 연결 성공" 0
    version=$(echo "$response" | grep -o '"version":"[^"]*"' | cut -d'"' -f4)
    echo "  버전: $version"
else
    print_test_result "서버 연결 실패" 1
    echo "  스크립트를 종료합니다."
    exit 1
fi
echo ""

# 테스트 2: 헬스 체크 (클러스터 상태)
echo -e "${YELLOW}[테스트 2] 클러스터 상태 확인${NC}"
response=$(call_api GET "/cluster")
if echo "$response" | grep -q "ok"; then
    print_test_result "클러스터 상태 정상" 0
else
    print_test_result "클러스터 상태 확인 실패" 1
fi
echo ""

# 테스트 3: 컬렉션 생성
echo -e "${YELLOW}[테스트 3] 컬렉션 생성${NC}"
create_data='{
  "vectors": {
    "size": 384,
    "distance": "Cosine"
  }
}'
response=$(call_api PUT "/collections/${TEST_COLLECTION}" "$create_data")
if echo "$response" | grep -q '"result":true'; then
    print_test_result "컬렉션 생성 성공: ${TEST_COLLECTION}" 0
else
    print_test_result "컬렉션 생성 실패" 1
fi
echo ""

# 테스트 4: 컬렉션 목록 조회
echo -e "${YELLOW}[테스트 4] 컬렉션 목록 조회${NC}"
response=$(call_api GET "/collections")
if echo "$response" | grep -q "${TEST_COLLECTION}"; then
    print_test_result "컬렉션 목록 조회 성공" 0
else
    print_test_result "컬렉션 목록 조회 실패" 1
fi
echo ""

# 테스트 5: 컬렉션 정보 조회
echo -e "${YELLOW}[테스트 5] 컬렉션 정보 조회${NC}"
response=$(call_api GET "/collections/${TEST_COLLECTION}")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "컬렉션 정보 조회 성공" 0
    echo "  벡터 크기: $(echo "$response" | grep -o '"size":[0-9]*' | head -1 | cut -d':' -f2)"
    echo "  거리 측정: $(echo "$response" | grep -o '"distance":"[^"]*"' | head -1 | cut -d'"' -f4)"
else
    print_test_result "컬렉션 정보 조회 실패" 1
fi
echo ""

# 테스트 6: 포인트(벡터) 삽입
echo -e "${YELLOW}[테스트 6] 벡터 데이터 삽입${NC}"
insert_data='{
  "points": [
    {
      "id": 1,
      "vector": '$(python3 -c "import random; print([random.random() for _ in range(384)])")',
      "payload": {"city": "Seoul", "country": "Korea"}
    },
    {
      "id": 2,
      "vector": '$(python3 -c "import random; print([random.random() for _ in range(384)])")',
      "payload": {"city": "Busan", "country": "Korea"}
    },
    {
      "id": 3,
      "vector": '$(python3 -c "import random; print([random.random() for _ in range(384)])")',
      "payload": {"city": "Tokyo", "country": "Japan"}
    }
  ]
}'
response=$(call_api PUT "/collections/${TEST_COLLECTION}/points" "$insert_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "벡터 데이터 삽입 성공 (3개)" 0
else
    print_test_result "벡터 데이터 삽입 실패" 1
fi
echo ""

# 잠시 대기 (인덱싱 완료 대기)
sleep 1

# 테스트 7: 포인트 조회
echo -e "${YELLOW}[테스트 7] 특정 포인트 조회${NC}"
response=$(call_api GET "/collections/${TEST_COLLECTION}/points/1")
if echo "$response" | grep -q '"id":1'; then
    print_test_result "포인트 조회 성공" 0
    echo "  ID: 1"
    echo "  Payload: $(echo "$response" | grep -o '"payload":{[^}]*}' | head -1)"
else
    print_test_result "포인트 조회 실패" 1
fi
echo ""

# 테스트 8: 벡터 검색
echo -e "${YELLOW}[테스트 8] 유사도 벡터 검색${NC}"
search_data='{
  "vector": '$(python3 -c "import random; print([random.random() for _ in range(384)])")',
  "limit": 3,
  "with_payload": true
}'
response=$(call_api POST "/collections/${TEST_COLLECTION}/points/search" "$search_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "벡터 검색 성공" 0
    result_count=$(echo "$response" | grep -o '"id":[0-9]*' | wc -l)
    echo "  검색 결과: ${result_count}개"
else
    print_test_result "벡터 검색 실패" 1
fi
echo ""

# 테스트 9: 필터링 검색
echo -e "${YELLOW}[테스트 9] 필터링 검색 (country=Korea)${NC}"
filter_search_data='{
  "vector": '$(python3 -c "import random; print([random.random() for _ in range(384)])")',
  "limit": 10,
  "filter": {
    "must": [
      {
        "key": "country",
        "match": {
          "value": "Korea"
        }
      }
    ]
  },
  "with_payload": true
}'
response=$(call_api POST "/collections/${TEST_COLLECTION}/points/search" "$filter_search_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "필터링 검색 성공" 0
    korea_count=$(echo "$response" | grep -o '"country":"Korea"' | wc -l)
    echo "  한국 도시 검색 결과: ${korea_count}개"
else
    print_test_result "필터링 검색 실패" 1
fi
echo ""

# 테스트 10: 포인트 업데이트
echo -e "${YELLOW}[테스트 10] 포인트 페이로드 업데이트${NC}"
update_data='{
  "points": [1],
  "payload": {
    "population": 9776000,
    "updated": true
  }
}'
response=$(call_api POST "/collections/${TEST_COLLECTION}/points/payload" "$update_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "페이로드 업데이트 성공" 0
else
    print_test_result "페이로드 업데이트 실패" 1
fi
echo ""

# 테스트 11: 스크롤 (전체 포인트 조회)
echo -e "${YELLOW}[테스트 11] 스크롤 (전체 포인트 조회)${NC}"
scroll_data='{"limit": 10, "with_payload": true, "with_vector": false}'
response=$(call_api POST "/collections/${TEST_COLLECTION}/points/scroll" "$scroll_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "스크롤 조회 성공" 0
    total_points=$(echo "$response" | grep -o '"id":[0-9]*' | wc -l)
    echo "  총 포인트: ${total_points}개"
else
    print_test_result "스크롤 조회 실패" 1
fi
echo ""

# 테스트 12: 포인트 삭제
echo -e "${YELLOW}[테스트 12] 특정 포인트 삭제${NC}"
delete_data='{"points": [3]}'
response=$(call_api POST "/collections/${TEST_COLLECTION}/points/delete" "$delete_data")
if echo "$response" | grep -q '"status":"ok"'; then
    print_test_result "포인트 삭제 성공 (ID: 3)" 0
else
    print_test_result "포인트 삭제 실패" 1
fi
echo ""

# 테스트 13: 컬렉션 통계
echo -e "${YELLOW}[테스트 13] 컬렉션 통계 조회${NC}"
response=$(call_api GET "/collections/${TEST_COLLECTION}")
if echo "$response" | grep -q '"points_count"'; then
    print_test_result "컬렉션 통계 조회 성공" 0
    points_count=$(echo "$response" | grep -o '"points_count":[0-9]*' | cut -d':' -f2)
    echo "  현재 포인트 수: ${points_count}"
else
    print_test_result "컬렉션 통계 조회 실패" 1
fi
echo ""

# 테스트 14: 스냅샷 생성
echo -e "${YELLOW}[테스트 14] 스냅샷 생성${NC}"
response=$(call_api POST "/collections/${TEST_COLLECTION}/snapshots" "")
if echo "$response" | grep -q '"name"'; then
    print_test_result "스냅샷 생성 성공" 0
    snapshot_name=$(echo "$response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
    echo "  스냅샷: ${snapshot_name}"
else
    print_test_result "스냅샷 생성 실패" 1
fi
echo ""

# 테스트 15: 컬렉션 삭제 (정리)
if [ "$DELETE_COLLECTION" = true ]; then
    echo -e "${YELLOW}[테스트 15] 테스트 컬렉션 삭제${NC}"
    response=$(call_api DELETE "/collections/${TEST_COLLECTION}")
    if echo "$response" | grep -q '"result":true'; then
        print_test_result "컬렉션 삭제 성공" 0
    else
        print_test_result "컬렉션 삭제 실패" 1
    fi
    echo ""
else
    echo -e "${YELLOW}[테스트 15] 테스트 컬렉션 유지${NC}"
    echo -e "${GREEN}✓${NC} 컬렉션 '${TEST_COLLECTION}'이(가) 보존되었습니다."
    echo "  삭제하려면 -d 옵션을 사용하세요."
    echo ""
fi

# 최종 결과 출력
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}           테스트 결과 요약${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}통과: ${PASSED}${NC}"
echo -e "${RED}실패: ${FAILED}${NC}"
echo -e "총 테스트: $((PASSED + FAILED))"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 모든 테스트가 통과했습니다!${NC}"
    exit 0
else
    echo -e "${RED}✗ 일부 테스트가 실패했습니다.${NC}"
    exit 1
fi
