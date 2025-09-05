#!/bin/bash

# 서버에서 실행되는 배포 스크립트 (빌드된 JAR 파일 배포용)
# 로컬에서 build-and-deploy.sh로 호출됨

set -e

echo "🚀 서버 배포 시작..."

# 변수 설정
APP_NAME="greenmate-ai-agent"
JAR_NAME="route-recommendation-0.0.1-SNAPSHOT.jar"
APP_DIR="/opt/greenmate"
NGINX_CONF="/etc/nginx/sites-available/greenmate"
SERVICE_NAME="greenmate-ai"
CURRENT_DIR=$(pwd)

# Gemini API 키 확인
if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  경고: GEMINI_API_KEY 환경변수가 설정되지 않았습니다."
    echo "   다음 명령으로 설정하세요: export GEMINI_API_KEY='your-api-key'"
fi

# 1. 서비스 중지 (이미 실행 중인 경우)
echo "🛑 기존 서비스 중지 중..."
systemctl stop $SERVICE_NAME 2>/dev/null || true
pkill -f $JAR_NAME 2>/dev/null || true

# 2. 애플리케이션 디렉토리 준비
echo "📁 애플리케이션 디렉토리 준비 중..."
mkdir -p $APP_DIR
mkdir -p /var/log/greenmate

# 3. JAR 파일 복사
echo "📂 JAR 파일 배포 중..."
if [ ! -f "$CURRENT_DIR/$JAR_NAME" ]; then
    echo "❌ 오류: $JAR_NAME 파일을 찾을 수 없습니다."
    exit 1
fi

cp $CURRENT_DIR/$JAR_NAME $APP_DIR/

# 사용자 권한 설정 (시스템에 맞게 조정)
if id "www-data" &>/dev/null; then
    chown -R www-data:www-data $APP_DIR
    SERVICE_USER="www-data"
    SERVICE_GROUP="www-data"
elif id "nginx" &>/dev/null; then
    chown -R nginx:nginx $APP_DIR
    SERVICE_USER="nginx"
    SERVICE_GROUP="nginx"
else
    # root로 실행
    SERVICE_USER="root"
    SERVICE_GROUP="root"
    echo "⚠️  경고: www-data 또는 nginx 사용자가 없어 root로 실행합니다."
fi

chmod +x $APP_DIR/$JAR_NAME

# 4. systemd 서비스 파일 생성
echo "⚙️ systemd 서비스 설정 중..."
tee /etc/systemd/system/$SERVICE_NAME.service > /dev/null <<EOF
[Unit]
Description=GreenMate AI Agent Service
After=network.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_GROUP
WorkingDirectory=$APP_DIR
ExecStart=/usr/lib/jvm/java-21-openjdk-21.0.8.0.9-1.el8.x86_64/bin/java -jar -Dspring.profiles.active=prod -Xmx512m $APP_DIR/$JAR_NAME
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment=GEMINI_API_KEY=\${GEMINI_API_KEY}

[Install]
WantedBy=multi-user.target
EOF

# 5. nginx 설정 (파일이 존재하는 경우)
if [ -f "$CURRENT_DIR/nginx.conf" ]; then
    echo "🌐 nginx 설정 중..."
    cp $CURRENT_DIR/nginx.conf $NGINX_CONF
    ln -sf $NGINX_CONF /etc/nginx/sites-enabled/ 2>/dev/null || true
    
    # nginx 설정 테스트
    if nginx -t; then
        echo "✅ nginx 설정 검증 완료"
    else
        echo "❌ nginx 설정 오류"
        exit 1
    fi
else
    echo "⚠️ nginx.conf 파일이 없습니다. nginx 설정을 건너뜁니다."
fi

# 6. 서비스 시작
echo "🟢 서비스 시작 중..."
systemctl daemon-reload
systemctl enable $SERVICE_NAME
systemctl start $SERVICE_NAME

# 7. nginx 재시작 (설정이 변경된 경우)
if [ -f "$CURRENT_DIR/nginx.conf" ]; then
    echo "🔄 nginx 재시작 중..."
    systemctl restart nginx
fi

# 8. 상태 확인
sleep 3
echo "📊 서비스 상태 확인 중..."
if systemctl is-active --quiet $SERVICE_NAME; then
    echo "✅ $SERVICE_NAME 서비스 실행 중"
else
    echo "❌ $SERVICE_NAME 서비스 시작 실패"
    systemctl status $SERVICE_NAME --no-pager -l
    exit 1
fi

if systemctl is-active --quiet nginx; then
    echo "✅ nginx 서비스 실행 중"
else
    echo "❌ nginx 서비스 오류"
    systemctl status nginx --no-pager
fi

echo ""
echo "🎉 서버 배포 완료!"
echo "📡 API 서버: http://103.244.108.70"
echo "🔍 헬스체크: http://103.244.108.70/health"
echo ""
echo "📋 관리 명령어:"
echo "  서비스 상태: systemctl status $SERVICE_NAME"
echo "  서비스 로그: journalctl -u $SERVICE_NAME -f"
echo "  서비스 재시작: systemctl restart $SERVICE_NAME"
echo ""