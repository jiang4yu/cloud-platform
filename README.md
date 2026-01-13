# 项目文档
## 组件版本
| SpringCloudAlibaba | SpringCloud | SpringBoot | Nacos | Sentinel | RocketMQ | Seata |
|--------------------|-------------|------------|-------|----------|----------|-------|
| 2021.0.5.0         | 2021.0.5    | 2.6.13     | 2.2.0 | 1.8.6    | 4.9.4    | 1.6.1 |

## 服务
| 服务标识                     | 服务名称     | 服务描述  |
|--------------------------|----------|-------|
| cloud-platform-gateway   | 网关       | 9999  |
| cloud-platform-system    | 系统管理模块   | 10010 |
| cloud-platform-mdm       | 主数据服务    | 10020 |
| cloud-platform-equipment | 设备管理模块   | 10030 |
| cloud-platform-produce   | 生产管理模块   | 10040 |
| cloud-platform-warehouse | 仓库管理模块   | 10050 |
| cloud-platform-quality   | 质量管理模块   | 10060 |
| cloud-platform-zlm       | zlm流媒体服务 | 10070 |
| cloud-platform-admin     | 监控服务     | 10000 |

## 项目
| 项目标识                        | 项目名称 | 项目端口  |
|-----------------------------|------|-------|
| cloud-platform-alarm        | 设备告警 | 20010 |
| cloud-platform-diary        | 日记项目 | 20020 |
| cloud-platform-friendcircle | 朋友圈啊 | 20030 |
| cloud-platform-missav       | 视频网站 | 20040 |
| cloud-platform-news         | 新闻网站 | 20050 |
| cloud-platform-novel        | 小说网站 | 20060 |

## 中间件
| 中间件          | 描述        | 端口         | 访问界面                    |
|--------------|-----------|------------|-------------------------|
| nacos        | 服务注册、配置管理 | 8848       | http://localhost:8848/  |
| mysql        | 关系型数据库    | 3306       |                         |
| pgsql        | 关系型数据库    | 5432       |                         |
| redis        | 分布式缓存     | 6379       |                         |
| xxl-job      | 分布式定时任务   | 8080       | http://localhost:8080/xxl-job-admin/  |
| rabbitmq     | 消息队列      | 5672、15672 | http://localhost:15672/ |
| minio        | 文件存储      | 9000、9001  | http://localhost:9001/browser |
| elasticsearch | 分布式搜索引擎   | 9200、9300      | http://localhost:9200/ |
| mongodb      | 非关系型数据库   | 27017       |  |

### 清除端口占用进程
```
netstat -aon | findstr <PORT>
taskkill /F /PID <PID>
```

### 解决Docker端口被占用问题
```angular2html
(HTTP code 500) server error - ports are not available: exposing port TCP 0.0.0.0:10010 -> 127.0.0.1:0: listen tcp 0.0.0.0:10010: 
bind: An attempt was made to access a socket in a way forbidden by its access permissions.
```
```angular2html
netstat -ano | findstr 6041
net stop winnat
net start winnat
```

### Docker常用命令
```dockerfile
# 查看正在运行的容器
docker ps

# 查看所有容器（包括停止的）
docker ps -a

# 查看容器简要信息（ID和名称）
docker ps -aq

docker run [参数] 镜像名 [容器内命令]

# 后台启动nginx容器，映射80端口，命名为my-nginx
docker run -d -p 80:80 --name my-nginx nginx

# 交互式启动ubuntu容器（进入bash终端）
docker run -it --name my-ubuntu ubuntu /bin/bash

# 启动已停止的容器
docker start 容器名/ID

# 停止运行中的容器
docker stop 容器名/ID

# 强制停止容器（类似断电）
docker kill 容器名/ID

# 重启容器
docker restart 容器名/ID

# 进入容器并打开交互式终端（推荐，支持退出后容器继续运行）
docker exec -it 容器名/ID /bin/bash  # 或 /bin/sh（根据容器内shell类型）

# 示例：进入my-nginx容器
docker exec -it my-nginx /bin/bash

# 查看容器详细配置（IP、网络、挂载等）
docker inspect 容器名/ID

# 查看容器日志（实时输出）
docker logs -f 容器名/ID

# 查看最近100行日志
docker logs --tail 100 容器名/ID

# 删除已停止的容器
docker rm 容器名/ID

# 强制删除运行中的容器
docker rm -f 容器名/ID

# 批量删除所有停止的容器
docker rm $(docker ps -aq)

docker pull 镜像名:标签  # 标签不指定则默认拉取latest（最新版）
# 示例：拉取EMQX 5.8.8镜像
docker pull emqx:5.8.8

docker images  # 列出所有本地镜像
docker images 镜像名  # 过滤指定镜像

docker rmi 镜像名:标签/镜像ID  # 删除指定镜像
docker rmi -f 镜像ID  # 强制删除（即使有容器依赖）

# 批量删除无标签的镜像（<none>）
docker rmi $(docker images -f "dangling=true" -q)

docker build -t 镜像名:标签 构建目录  # -t指定镜像名称和标签
# 示例：在当前目录（.）构建名为my-app:v1的镜像
docker build -t my-app:v1 .

# 保存镜像为tar文件
docker save -o 文件名.tar 镜像名:标签
# 示例：保存emqx:5.8.8为emqx.tar
docker save -o emqx.tar emqx:5.8.8

# 从tar文件加载镜像
docker load -i 文件名.tar

docker network ls  # 列出所有网络
docker network inspect 网络名/ID  # 查看网络详情（含关联容器）

docker network create --driver bridge 网络名  # 创建自定义桥接网络
# 示例：创建名为mqtt-net的网络
docker network create mqtt-net

# 让容器连接到指定网络
docker network connect 网络名 容器名/ID

# 让容器断开指定网络
docker network disconnect 网络名 容器名/ID

docker volume ls  # 列出所有数据卷
docker volume inspect 卷名  # 查看卷详情（挂载路径等）

docker volume create 卷名  # 创建数据卷
docker volume rm 卷名  # 删除数据卷

# 清理无容器使用的卷（孤儿卷）
docker volume prune

docker info  # 查看Docker引擎详细信息（版本、容器数、镜像数等）
docker version  # 查看Docker客户端和服务端版本

# 主机 → 容器：docker cp 主机文件路径 容器名:容器内路径
docker cp ./test.txt my-nginx:/tmp/

# 容器 → 主机：docker cp 容器名:容器内文件路径 主机路径
docker cp my-nginx:/etc/nginx/nginx.conf ./

docker rename 旧容器名 新容器名

docker stats  # 实时查看所有容器的CPU、内存占用
docker stats 容器名/ID  # 查看指定容器的资源占用
```

### git切换分支并推送远程分支
```shell
# 步骤1: 在当前main或master分支上的修改暂存起来
git stash
# 步骤2: 修改暂存后，在本地新建分支（new_branch为新分支名称）
git checkout -b new_branch
# 步骤3: 将暂存的修改放入新分支中
git stash pop
# 步骤4: 在本地新分支中进行commit，比如：add、update、delete
git commit -m "message"
# 步骤5：将提交的内容push到远程分支
git push
```