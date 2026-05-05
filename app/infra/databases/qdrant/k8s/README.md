# Qdrant on Kubernetes (EKS) 배포 가이드

AWS EKS 환경에서 Qdrant Vector Database를 Helm을 사용하여 배포하기 위한 설정 파일과 스크립트 모음입니다.

## 파일 구조

```
k8s/
├── README.md                 # 이 문서
├── values-dev.yaml          # 개발 환경 설정
├── values-prod.yaml         # 운영 환경 설정
├── install.sh               # Helm 설치 스크립트
└── create-api-key.sh        # API Key Secret 생성 스크립트
```

## 주요 특징

### 개발 환경 (values-dev.yaml)
- **Replica**: 1개 (단일 인스턴스)
- **Storage**: EFS 50Gi (RWX)
- **Resources**: 500Mi~1Gi 메모리, 500m~1000m CPU
- **API Key**: 직접 값 설정 가능
- **Ingress**: NGINX Ingress Controller
- **TLS**: Let's Encrypt 자동 인증서
- **Image**: `latest` (자동 업데이트)

### 운영 환경 (values-prod.yaml)
- **Replica**: 3개 (고가용성)
- **Storage**: EFS 200Gi (RWX)
- **Resources**: 4Gi~8Gi 메모리, 1000m~4000m CPU
- **API Key**: Kubernetes Secret 사용 (보안)
- **Cluster Mode**: 활성화 (수평 확장)
- **Monitoring**: Prometheus ServiceMonitor
- **Pod Disruption Budget**: 최소 1개 유지
- **Multi-AZ**: 가용 영역 분산 배치
- **Image**: `v1.7.4` (고정 버전)

## 사전 요구사항

### 1. EKS 클러스터 준비
```bash
# 클러스터 컨텍스트 확인
kubectl config current-context
```

### 2. EFS Storage Class 생성
```bash
# EFS CSI Driver 설치 확인
kubectl get csidriver efs.csi.aws.com

# StorageClass 생성 (예시)
cat <<EOF | kubectl apply -f -
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: efs-sc-shared
provisioner: efs.csi.aws.com
parameters:
  provisioningMode: efs-ap
  fileSystemId: fs-xxxxxxxxx  # 실제 EFS ID로 변경
  directoryPerms: "700"
EOF
```

### 3. NGINX Ingress Controller 설치
```bash
# Helm으로 설치
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer
```

### 4. cert-manager 설치 (TLS 인증서 자동 관리)
```bash
helm repo add jetstack https://charts.jetstack.io
helm repo update
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true

# ClusterIssuer 생성
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com  # 이메일 변경 필요
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

## 설치 방법

### 방법 1: 스크립트 사용 (권장)

#### 개발 환경 설치
```bash
cd k8s
chmod +x install.sh
./install.sh dev
```

#### 운영 환경 설치
```bash
./install.sh prod
```

### 방법 2: 수동 설치

#### 1. Helm Repository 추가
```bash
helm repo add qdrant https://qdrant.github.io/qdrant-helm
helm repo update
```

#### 2. Namespace 생성
```bash
kubectl create namespace qdrant
```

#### 3. API Key Secret 생성 (운영 환경)
```bash
chmod +x create-api-key.sh
./create-api-key.sh qdrant prod

# 생성된 API Key를 안전하게 보관!
```

#### 4. Helm 설치
```bash
# 개발 환경
helm upgrade --install qdrant qdrant/qdrant \
  --namespace qdrant \
  --values values-dev.yaml \
  --wait

# 운영 환경 (API Key Secret 사용)
helm upgrade --install qdrant qdrant/qdrant \
  --namespace qdrant \
  --values values-prod.yaml \
  --set apiKey.enabled=true \
  --set apiKey.existingSecret=qdrant-api-key-prod \
  --wait
```

## 설정 커스터마이징

### 도메인 변경
`values-dev.yaml` 또는 `values-prod.yaml`에서 수정:
```yaml
ingress:
  hosts:
    - host: qdrant.your-domain.com  # 여기를 변경
  tls:
    - secretName: qdrant-tls-secret
      hosts:
        - qdrant.your-domain.com  # 여기를 변경
```

### API Key 설정

#### 개발 환경 (직접 입력)
```yaml
apiKey:
  enabled: true
  value: "your-dev-api-key"
  existingSecret: ""
```

#### 운영 환경 (Secret 사용)
```yaml
apiKey:
  enabled: true
  value: ""
  existingSecret: "qdrant-api-key-prod"
  existingSecretKey: "api-key"
```

### Storage 크기 조정
```yaml
persistence:
  size: 100Gi  # 필요한 크기로 변경
```

### Resource 조정
```yaml
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "4Gi"
    cpu: "2000m"
```

## 설치 확인

### 1. Pod 상태 확인
```bash
kubectl get pods -n qdrant
```

예상 출력:
```
NAME        READY   STATUS    RESTARTS   AGE
qdrant-0    1/1     Running   0          2m
```

### 2. Service 확인
```bash
kubectl get svc -n qdrant
```

### 3. Ingress 확인
```bash
kubectl get ingress -n qdrant
```

### 4. 자동 테스트 스크립트 실행 (권장)

포괄적인 기능 테스트를 자동으로 수행하는 스크립트를 제공합니다.

#### Ingress를 통한 테스트 (기본)
```bash
cd k8s
chmod +x qdrant-test.sh
./qdrant-test.sh

# API Key 사용
./qdrant-test.sh -k 'Skala25a!23$'
```

#### Port Forward를 통한 테스트
```bash
# Port-forward 모드로 전환
./qdrant-test.sh -p

# Port-forward + API Key 사용
./qdrant-test.sh -p -k 'Skala25a!23$'
```

#### 테스트 스크립트 옵션
```bash
# 사용법
./qdrant-test.sh [OPTIONS]

OPTIONS:
  -n, --namespace NAMESPACE    Kubernetes namespace (기본값: qdrant)
  -s, --service SERVICE        Service 이름 (기본값: qdrant)
  -k, --api-key API_KEY        API Key (설정된 경우)
  -p, --port-forward           Port-forward 사용 (기본값: Ingress)
  -h, --help                   도움말 표시

예시:
  ./qdrant-test.sh                      # Ingress URL로 테스트 (기본)
  ./qdrant-test.sh -p                   # Port-forward로 테스트
  ./qdrant-test.sh -k 'api-key'         # Ingress + API Key
  ./qdrant-test.sh -p -k 'api-key'      # Port-forward + API Key
```

#### 테스트 항목 (15개)
1. ✓ 서버 연결 확인
2. ✓ 클러스터 상태 확인
3. ✓ 컬렉션 생성
4. ✓ 컬렉션 목록 조회
5. ✓ 컬렉션 정보 조회
6. ✓ 벡터 데이터 삽입 (3개)
7. ✓ 특정 포인트 조회
8. ✓ 유사도 벡터 검색
9. ✓ 필터링 검색 (country=Korea)
10. ✓ 포인트 페이로드 업데이트
11. ✓ 스크롤 (전체 포인트 조회)
12. ✓ 특정 포인트 삭제
13. ✓ 컬렉션 통계 조회
14. ✓ 스냅샷 생성
15. ✓ 테스트 컬렉션 삭제

### 5. 수동 테스트

#### Port Forward로 로컬 테스트
```bash
kubectl port-forward -n qdrant svc/qdrant 6333:6333
```

브라우저에서 접속:
- Dashboard: http://localhost:6333/dashboard
- Health Check: http://localhost:6333/

#### Ingress를 통한 접속
```bash
# HTTPS 연결 (TLS 인증서 설정 완료 시)
curl https://qdrant.skala25a.project.skala-ai.com/

# Dashboard
# https://qdrant.skala25a.project.skala-ai.com/dashboard
```

### 6. API 테스트 (API Key 있는 경우)
```bash
# Port Forward 사용 시
curl -H "api-key: Skala25a!23$" http://localhost:6333/
curl -H "api-key: Skala25a!23$" http://localhost:6333/collections

# Ingress 사용 시
curl -H "api-key: Skala25a!23$" https://qdrant.skala25a.project.skala-ai.com/
curl -H "api-key: Skala25a!23$" https://qdrant.skala25a.project.skala-ai.com/collections
```

## 모니터링 및 로그

### Pod 로그 확인
```bash
# 실시간 로그
kubectl logs -f -n qdrant qdrant-0

# 최근 100줄
kubectl logs --tail=100 -n qdrant qdrant-0
```

### Describe Pod
```bash
kubectl describe pod -n qdrant qdrant-0
```

### PVC 확인
```bash
kubectl get pvc -n qdrant
```

### Metrics (운영 환경)
```bash
# ServiceMonitor 확인
kubectl get servicemonitor -n qdrant

# Prometheus에서 메트릭 확인
# qdrant_* 메트릭 사용 가능
```

## 업그레이드

### Helm Chart 업그레이드
```bash
# Helm repo 업데이트
helm repo update

# 업그레이드 (설정 파일 유지)
helm upgrade qdrant qdrant/qdrant \
  --namespace qdrant \
  --values values-dev.yaml \
  --wait
```

### 이미지 버전 변경
```bash
# values.yaml 수정 또는 직접 지정
helm upgrade qdrant qdrant/qdrant \
  --namespace qdrant \
  --values values-prod.yaml \
  --set image.tag=v1.8.0 \
  --wait
```

## 삭제

### Helm Release 삭제
```bash
helm uninstall qdrant -n qdrant
```

### Namespace 삭제 (주의: 데이터 손실)
```bash
# PVC도 함께 삭제됨
kubectl delete namespace qdrant
```

### PVC만 별도 삭제
```bash
kubectl delete pvc -n qdrant --all
```

## 보안 권장사항

### 1. API Key 관리
- ✅ 운영 환경에서는 반드시 Kubernetes Secret 사용
- ✅ API Key를 Git에 커밋하지 않기
- ✅ 정기적으로 API Key 교체
- ✅ AWS Secrets Manager 또는 External Secrets Operator 고려

### 2. Network Policy
```yaml
# 예시: Qdrant에 특정 Namespace에서만 접근 허용
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: qdrant-allow
  namespace: qdrant
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: qdrant
  policyTypes:
  - Ingress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: application
```

### 3. RBAC 설정
```bash
# 최소 권한 원칙 적용
kubectl create role qdrant-user --verb=get,list --resource=pods,services -n qdrant
```

### 4. Ingress 보안
- ✅ TLS 필수 사용
- ✅ Basic Auth 또는 OAuth 프록시 추가 고려
- ✅ IP 화이트리스트 설정 (내부망 전용)

## 성능 튜닝

### 1. JVM 힙 메모리 (운영 환경)
```yaml
config:
  storage:
    performance:
      max_search_threads: 0  # CPU 코어 수만큼 자동
```

### 2. EFS 성능 모드
- **General Purpose**: 일반적인 워크로드
- **Max I/O**: 높은 처리량 필요 시 (운영 환경 권장)

### 3. Replica 수 조정
```yaml
# 부하에 따라 조정
replicaCount: 5  # 운영 환경
```

## 문제 해결

### Pod가 Pending 상태
```bash
# 원인 확인
kubectl describe pod -n qdrant qdrant-0

# StorageClass 확인
kubectl get storageclass
```

### PVC Binding 실패
- EFS CSI Driver 설치 확인
- EFS File System ID 확인
- Security Group 및 Mount Target 확인

### Ingress 접속 불가
```bash
# Ingress Controller 확인
kubectl get pods -n ingress-nginx

# Ingress 상태 확인
kubectl describe ingress -n qdrant

# DNS 레코드 확인
nslookup qdrant.your-domain.com
```

### API Key 인증 실패
- Secret 존재 확인: `kubectl get secret -n qdrant`
- Pod 환경변수 확인: `kubectl exec -it qdrant-0 -n qdrant -- env | grep QDRANT`

## 추가 자료

- [Qdrant 공식 문서](https://qdrant.tech/documentation/)
- [Qdrant Helm Chart](https://github.com/qdrant/qdrant-helm)
- [AWS EFS CSI Driver](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)

## 라이센스

이 설정 파일들은 Apache 2.0 라이센스를 따릅니다.

## 기여

문제가 발생하거나 개선 사항이 있다면 이슈를 등록해주세요.
