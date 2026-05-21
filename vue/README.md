# 抖音精选 Web 前端

## 开发前准备

1. MySQL 已执行项目根目录 `douyin_schema.sql`
2. **必须先启动 Spring Boot 后端**（端口 8080），再启动本前端

## 启动（需要两个终端）

**终端 1 — 后端**

```sh
cd douyin-web-clone/springboot
mvn spring-boot:run
```

看到 `Started SpringbootApplication` 后再进行下一步。

若提示端口占用，先结束占用 8080 的进程，或执行 `mvn clean spring-boot:run`。

**终端 2 — 前端**

```sh
cd douyin-web-clone/vue
npm install
npm run dev
```

浏览器访问 Vite 给出的地址（一般为 http://localhost:5173 ）。

前端 `/api` 请求会代理到 `http://localhost:8080`。若出现 `ECONNREFUSED`，说明后端未启动或未监听 8080。

## 生产构建

```sh
npm run build
```
