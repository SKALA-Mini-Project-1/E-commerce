# 빌드 스테이지 - 컴파일 의존성이 포함된 환경
FROM python:3.11-slim AS builder

# pip 업그레이드
RUN python3 -m pip install --upgrade pip

# requirements 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 런타임 스테이지 - 실행에 필요한 최소한의 환경
FROM python:3.11-slim AS runtime

# 빌드 스테이지에서 Python 패키지들 복사
COPY --from=builder /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=builder /usr/local/bin /usr/local/bin
