#!/bin/bash

# GreenMate AI Agent 로컬 빌드 + 서버 배포 스크립트
# 사용법: ./build-and-deploy.sh

set -e

SERVER_IP="103.244.108.70"
SERVER_USER="root"
SERVER_PATH="/root/greenmate"
JAR_NAME="route-recommendation-0.0.1-SNAPSHOT.jar"

echo "🏗️ 로컬에서 JAR 빌드 시작..."

# 1. 로컬 빌드
echo "📦 JAR 파일 빌드 중..."
./gradlew clean build -x test

# 2. 빌드 결과 확인
if [ ! -f "build/libs/$JAR_NAME" ]; then
    echo "❌ JAR 빌드 실패: build/libs/$JAR_NAME 파일을 찾을 수 없습니다."
    exit 1
fi

echo "✅ JAR 빌드 완료: build/libs/$JAR_NAME"

# 3. 서버로 파일 전송
echo "📤 서버로 파일 전송 중..."

# 서버에 디렉토리 생성
ssh $SERVER_USER@$SERVER_IP "mkdir -p $SERVER_PATH"

# JAR 파일과 설정 파일들 전송
scp build/libs/$JAR_NAME $SERVER_USER@$SERVER_IP:$SERVER_PATH/
scp nginx.conf $SERVER_USER@$SERVER_IP:$SERVER_PATH/
scp server-deploy.sh $SERVER_USER@$SERVER_IP:$SERVER_PATH/

echo "✅ 파일 전송 완료"

# 4. 서버에서 배포 스크립트 실행
echo "🚀 서버에서 배포 실행 중..."
ssh $SERVER_USER@$SERVER_IP "cd $SERVER_PATH && chmod +x server-deploy.sh && ./server-deploy.sh"

echo ""
echo "🎉 배포 완료!"
echo "📡 API 서버: http://$SERVER_IP"
echo "🔍 헬스체크: http://$SERVER_IP/health"
echo ""