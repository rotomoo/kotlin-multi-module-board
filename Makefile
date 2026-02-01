.PHONY: all api front install clean

# .env 파일 로드 (.env.local이 .env를 오버라이드)
ifneq (,$(wildcard ./.env))
    include .env
    export
endif
ifneq (,$(wildcard ./.env.local))
    include .env.local
    export
endif

# 전체 실행 (API + Front 병렬)
all: install
	@make -j2 api front

# API 서버 실행
api:
	./gradlew :api:bootRun

# Front 서버 실행
front:
	cd front && npm run dev

# 의존성 설치
install:
	./gradlew build -x test
	cd front && npm install

# 빌드 정리
clean:
	./gradlew clean
	rm -rf front/node_modules front/dist
